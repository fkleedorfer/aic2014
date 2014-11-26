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
        String decrypted = cryptoService.decrypt(payload);
        if (msg.getHopsToGo() < 0) {
            throw new IllegalArgumentException("hopsToGo must not be <0");
        }
        ListenableFuture<String> msgFuture = null;
        Message timeoutMessage = null; //if we hit a timeout, send this message (if not null)
        if (msg.getHopsToGo() == 0) {
            //last hop: we expect that the decrypted string is a http request.
            logger.debug("/request: last hop, received payload {}", decrypted);
            logger.debug("/request: sending exit request asynchronously");
            msgFuture = this.asyncRequestService.sendExitRequestAndTunnelResponse(msg.getRecipient(), decrypted);
        } else {
            Message nextMsg = JsonUtils.fromJSON(decrypted);
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
        final DeferredResult deferredResult = new DeferredResult<Message>(messageTimeout * msg.getHopsToGo(), timeoutObject);
        msgFuture.addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onFailure(Throwable ex) {
                //TODO: this method actually isn't called when an exception is thrown in
                //an @Async-annotated method - spring doesn't support that. If we really want that
                //we have to manage a thread pool manually or devise a different way of throwing an
                //error back to the calling thread.
                logger.debug("/request: failure", ex);
                Message responseMessage = new Message();
                responseMessage.setChainId(msg.getChainId());
                if (ex instanceof OnionRoutingRequestException){
                    responseMessage.setMisbehavingNode(((OnionRoutingRequestException)ex).getMisbehavingNode());
                    responseMessage.setStatus(OnionStatus.ERROR);
                } else if (ex instanceof OnionRoutingTargetRequestException) {
                    responseMessage.setStatus(OnionStatus.TARGET_ERROR);
                } else {
                    responseMessage.setStatus(OnionStatus.ERROR);
                }
                //set a result (not an errorResult) so that the message is propagated back normally
                deferredResult.setResult(responseMessage);
            }

            @Override
            public void onSuccess(String resultMessageString) {
                //if an exception is thrown here, the framework calls onFailure() above
                logger.debug("/request: done. result: {}", resultMessageString);
                Message responseMessage = new Message();
                responseMessage.setChainId(msg.getChainId());
                responseMessage.setPayload(cryptoService.encrypt(resultMessageString, msg.getPublicKey()));
                deferredResult.setResult(responseMessage);
            }
        });
        return deferredResult;
    }

}
