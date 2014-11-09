package com.github.aic2014.onion.client;

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

/**
 * Controller for the http server running in the originator.
 * Processes the response received from the start node.
 */
@Controller
public class OriginatorController {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  @Autowired
  private PendingResponseService pendingResponseService;
  @Autowired
  private CryptoService cryptoService;

  @RequestMapping(
          value = "/response",
          method = RequestMethod.PUT
  )
  public ResponseEntity<String> receiveResponse(@RequestBody Message msg){
    logger.debug("response received: {}", msg);
    PendingResponse pendingResponse = pendingResponseService.getAndRemovePendingResponse(msg.getChainId());
    //set the response's value, unblocking clients waiting at the response's get() method
    String response = decryptResponse(msg, pendingResponse);
    logger.debug("decrypted response: {}", response);
    pendingResponse.getResponse().set(response);
    return new ResponseEntity<String>(HttpStatus.OK);
  }

  private String decryptResponse(Message msg, PendingResponse pendingResponse){
    for (int i = 1; i < pendingResponse.getChainLength(); i ++){
      String payload = this.cryptoService.decrypt(msg.getPayload());
      msg = JsonUtils.fromJSON(payload);
    }
    return this.cryptoService.decrypt(msg.getPayload());
  }

}
