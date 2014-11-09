package com.github.aic2014.onion.chainnode;


import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.json.JsonUtils;
import com.github.aic2014.onion.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

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

  RestTemplate restTemplate = new RestTemplate();

  @RequestMapping(value="/request", method = RequestMethod.PUT)
  public ResponseEntity<String> routeRequest(@RequestBody Message msg)
    throws IOException {
    logger.debug("received request with message {}", msg);
    String payload = msg.getPayload();
    String decrypted = cryptoService.decrypt(payload);
    if (msg.getHopsToGo() < 0){
      throw new IllegalArgumentException("hopsToGo must not be <0");
    }
    this.responseInfoService.addResponseInfo(msg.getId(), new ResponseInfo(msg.getSender(), msg.getPublicKey()));
    if (msg.getHopsToGo() == 0){
      //last hop: we expect that the decrypted string is a http request.
      String plaintext = this.cryptoService.decrypt(payload);
      logger.debug("/request: last hop, received payload {}", plaintext);
      logger.debug("/request: sending exit request asynchronously");
      this.asyncRequestService.sendExitRequestAndTunnelResponse(plaintext, msg.getId());
    } else {
      Message nextMsg = JsonUtils.fromJSON(decrypted);
      logger.debug("/request: sending chain request asynchronously");
      this.asyncRequestService.sendChainRequest(nextMsg);
    }
    logger.debug("/request: done");
    return new ResponseEntity<String>("request routed", HttpStatus.OK);
  }

  @RequestMapping(value="/response", method=RequestMethod.PUT)
  public ResponseEntity<String> routeResponse(@RequestBody Message msg){
    logger.debug("/response: received message: ", msg);
    Message nextMessage = new Message();
    nextMessage.setPayload(this.cryptoService.encrypt(JsonUtils.toJSON(msg)));
    nextMessage.setId(msg.getId());
    logger.debug("/response: sending chain response asynchronously");
    this.asyncRequestService.sendChainResponse(nextMessage);
    logger.debug("/response: done");
    return new ResponseEntity<String>(HttpStatus.OK);
  }



}
