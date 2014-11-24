package com.github.aic2014.onion.chainnode;


import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.json.JsonUtils;
import com.github.aic2014.onion.model.Message;
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
public class ChainNodeController
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  CryptoService cryptoService;
  @Autowired
  ResponseInfoService responseInfoService;
  @Autowired
  AsyncRequestService asyncRequestService;
  @Value("${messageTimeout}")
  long messageTimeout;

  RestTemplate restTemplate = new RestTemplate();


  @RequestMapping(value="/request", method = RequestMethod.POST)
  @ResponseBody
  public DeferredResult<Message> routeRequest(final @RequestBody Message msg)
    throws IOException {
    logger.debug("received request with message {}", msg);
    String payload = msg.getPayload();
    String decrypted = cryptoService.decrypt(payload);
    if (msg.getHopsToGo() < 0){
      throw new IllegalArgumentException("hopsToGo must not be <0");
    }
    ListenableFuture<String> msgFuture = null;
    if (msg.getHopsToGo() == 0){
      //last hop: we expect that the decrypted string is a http request.
      logger.debug("/request: last hop, received payload {}", decrypted);
      logger.debug("/request: sending exit request asynchronously");
      msgFuture = this.asyncRequestService.sendExitRequestAndTunnelResponse(decrypted);
    } else {
      Message nextMsg = JsonUtils.fromJSON(decrypted);
      logger.debug("/request: sending chain request asynchronously");
      msgFuture = this.asyncRequestService.sendChainRequest(nextMsg);
    }

    //TODO: implement the onTimeout method of the DeferredResult to return
    //      a nice message that the client will be able to handle
    final DeferredResult deferredResult = new DeferredResult<Message>(messageTimeout);
    msgFuture.addCallback(new ListenableFutureCallback<String>() {
        @Override
        public void onFailure(Throwable ex) {
            //TODO: handle failure
            logger.debug("/request: failure", ex);
        }

        @Override
        public void onSuccess(String resultMessageString) {
            logger.debug("/request: done");
            Message responseMessage = new Message();
            responseMessage.setChainId(msg.getChainId());
            responseMessage.setPayload(cryptoService.encrypt(resultMessageString, msg.getPublicKey()));
            deferredResult.setResult(responseMessage);
        }
    });
    return deferredResult;
  }

}
