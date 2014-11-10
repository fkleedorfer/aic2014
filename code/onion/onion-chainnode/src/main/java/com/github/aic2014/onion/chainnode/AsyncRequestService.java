package com.github.aic2014.onion.chainnode;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Service responsible for asynchonously executing the
 * http requests (for each hop in the chain and for the exit)
 * and for feeding the http response back into the chain.
 */
@Service
public class AsyncRequestService {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  @Autowired
  private ResponseInfoService responseInfoService;
  @Autowired
  private CryptoService cryptoService;

  private RestTemplate restTemplate = new RestTemplate();

  @Async
  public void sendExitRequestAndTunnelResponse(String request, UUID chainId){
    logger.debug("sending tunneled http request {} ... NOT![still mocking this functionality]", request);
    //TODO: execute http request, convert response into a string
    String response = "Exit node received this request: '" + request + "', currently not sending the request anywhere";
    Message responseMessage = new Message();
    ResponseInfo responseInfo = responseInfoService.getAndDeleteResponseInfo(chainId);
    if (responseInfo == null){
      throw new IllegalArgumentException("could not obtain ResponseInfo for id " + chainId);
    }
    responseMessage.setChainId(chainId);
    responseMessage.setPayload(this.cryptoService.encrypt(response, null));
    logger.debug("sending this response message back through the chain: {}", responseMessage);
    this.restTemplate.put(responseInfo.getSenderOfRequest() + "/response", responseMessage);
  }

  @Async
  public void sendChainRequest(Message msg) {
    logger.debug("sending chain request message: {}", msg);
    restTemplate.put(msg.getRecipient()+"/request", msg);
  }

  public void sendChainResponse(Message msg) {
    ResponseInfo responseInfo = this.responseInfoService.getAndDeleteResponseInfo(msg.getChainId());
    if (responseInfo == null){
      throw new IllegalStateException("cannot retrieve ResponseInfo");
    }
    logger.debug("/response: sending message: {}", msg);
    restTemplate.put(responseInfo.getSenderOfRequest() + "/response", msg);
  }
}