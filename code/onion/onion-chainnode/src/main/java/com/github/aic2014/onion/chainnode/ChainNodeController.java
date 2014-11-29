package com.github.aic2014.onion.chainnode;


import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.exception.OnionRoutingRequestException;
import com.github.aic2014.onion.exception.OnionRoutingTargetRequestException;
import com.github.aic2014.onion.json.JsonUtils;
import com.github.aic2014.onion.model.Message;
import com.github.aic2014.onion.model.OnionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;

/**
 * Controller that offers onion chain node functionality. It is
 * responsible for analyzing the request body, building the
 * next message to send. The actual sending of the message is
 * delegated to the AsyncRequestService.
 */
@Controller
public class ChainNodeController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    CryptoService cryptoService;
    @Autowired
    AsyncRequestService asyncRequestService;
    @Value("${messageTimeout}")
    long messageTimeout;

    RestTemplate restTemplate = new RestTemplate();


    @RequestMapping(value = "/request", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<Message> routeRequest(final @RequestBody Message msg)
            throws IOException {

        logger.debug("received request with message {}", msg);
        String payload = msg.getPayload();
        String decryptedPayload = cryptoService.decrypt(payload);
        if (msg.getHopsToGo() < 0) {
            throw new IllegalArgumentException("hopsToGo must not be <0");
        }
        ListenableFuture<Message> msgFuture = null;
        Message timeoutMessage = null; //if we hit a timeout, send this message (if not null)
        if (msg.getHopsToGo() == 0) {
            logger.info("received exit request from {}", msg.getSender());
            //last hop: we expect that the decrypted string is a http request.
            logger.debug("/request: last hop, received payload {}", decryptedPayload);
            logger.debug("/request: sending exit request asynchronously");
            msgFuture = this.asyncRequestService.sendExitRequestAndTunnelResponse(msg.getRecipient(), msg.getChainId(),msg.getPublicKey(),decryptedPayload);
        } else {
            Message nextMsg = JsonUtils.fromJSON(decryptedPayload);
            logger.info("received chain request from {} to {}", msg.getSender(), nextMsg.getRecipient());
            logger.debug("/request: sending chain request asynchronously");
            msgFuture = this.asyncRequestService.sendChainRequest(nextMsg);
            //if sending nextMsg yields a timeout, send the follwing message back through the chain
            timeoutMessage = new Message();
            timeoutMessage.setChainId(msg.getChainId());
            timeoutMessage.setStatus(OnionStatus.CHAIN_TIMEOUT);
            timeoutMessage.setMisbehavingNode(nextMsg.getRecipient());
        }

        //DeferredResult treats a new Object as no object
        Object timeoutObject = timeoutMessage == null ? new Object() : timeoutMessage;
        final DeferredResult deferredResult = new DeferredResult<Message>(messageTimeout * (msg.getHopsToGo() + 1), timeoutObject);
        msgFuture.addCallback(new ListenableFutureCallback<Message>() {
            @Override
            public void onFailure(Throwable ex) {
                logger.debug("/request: failure", ex);
                Message responseMessage = new Message();
                responseMessage.setChainId(msg.getChainId());
                if (ex instanceof OnionRoutingRequestException){
                    responseMessage.setMisbehavingNode(((OnionRoutingRequestException)ex).getMisbehavingNode());
                    responseMessage.setStatus(OnionStatus.CHAIN_ERROR);
                } else if (ex instanceof OnionRoutingTargetRequestException) {
                    responseMessage.setStatus(OnionStatus.TARGET_ERROR);
                } else {
                    responseMessage.setStatus(OnionStatus.CHAIN_ERROR);
                }
                //set a result (not an errorResult) so that the message is propagated back normally
                logger.info("An error occurred during request processing, sending back an error message");
                deferredResult.setResult(responseMessage);
            }

            @Override
            public void onSuccess(Message responseMessage) {
                //if an exception is thrown here, the framework calls onFailure() above
                logger.info("received response, routing it back");
                logger.debug("/request: done. result: {}", responseMessage);
                deferredResult.setResult(responseMessage);
            }
        });
        return deferredResult;
    }

}
