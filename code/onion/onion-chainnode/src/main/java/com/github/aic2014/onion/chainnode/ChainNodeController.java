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
 * Controller that offers onion chain node functionality.
 */
@Controller
public class ChainNodeController
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  CryptoService cryptoService;
  RestTemplate restTemplate = new RestTemplate();

  @RequestMapping(value="/request", method = RequestMethod.PUT)
  public ResponseEntity<String> performOnionRoutingStep(@RequestBody Message msg)
    throws IOException {
    logger.debug("received request with message {}", msg);
    String payload = msg.getPayload();
    String decrypted = cryptoService.decrypt(payload);
    if (msg.getHopsToGo() == 0){
      //last hop: we expect that the decrypted string is a http request.
      logger.info("last hop, received payload {}", payload);
    } else {
      Message nextMsg = JsonUtils.fromJSON(decrypted);
      logger.info("hopsToGo: {}, sending message {}", msg.getHopsToGo(), nextMsg);
      restTemplate.put(nextMsg.getRecipient()+"/request", nextMsg);
    }
    return new ResponseEntity<String>("request routed", HttpStatus.OK);
  }


}
