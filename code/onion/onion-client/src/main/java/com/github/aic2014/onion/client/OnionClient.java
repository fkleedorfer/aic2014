package com.github.aic2014.onion.client;

import com.github.aic2014.onion.model.ChainNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;

/**
 * Client that accesses the directory node for obtaining a chain and then
 * sends an onion-routed request through the chain.
 */
public class OnionClient
{
  private final Logger logger = LoggerFactory.getLogger(getClass());


  private URI directoryNodeUri;

  private RestTemplate restTemplate = new RestTemplate();

  public OnionClient() {
  }

  private ChainNodeInfo[] getChain(){
    String requestUri = directoryNodeUri.toString() + "/getChain";
    logger.info("requesting: {}", requestUri);
      ResponseEntity<ChainNodeInfo[]> newChainResult = restTemplate.getForEntity(requestUri,  ChainNodeInfo[].class);
    ChainNodeInfo[] newChain = newChainResult.getBody();
    logger.debug("obtained new chain: {}", newChain);
    return newChain;
  }

  public void sendRequest(){
    //get the chain for the request
    ChainNodeInfo[] chain = getChain();
    logger.info("obtained chain {}", Arrays.toString(chain));
    //we're bootstrapping the functionality. At first, we just send a normal http request
    //to the first chain node, which responds with a dummy response.
    String result = restTemplate.getForObject(chain[0].getUri().toString() + "/route", String.class);
    logger.info("result obtained from chain node: {}", result);
  }

  public void setDirectoryNodeUri(final URI directoryNodeUri) {
    this.directoryNodeUri = directoryNodeUri;
  }
}
