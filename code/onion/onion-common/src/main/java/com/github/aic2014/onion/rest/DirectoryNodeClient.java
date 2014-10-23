package com.github.aic2014.onion.rest;

import com.github.aic2014.onion.model.ChainNodeInfo;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * REST client that can be used to talk to the directory node.
 */
public class DirectoryNodeClient
{

  private String directoryNodeURI;

  private RestTemplate restTemplate = new RestTemplate();

  public URI registerChainNode(ChainNodeInfo chainNodeInfo){
    return restTemplate.postForLocation(directoryNodeURI + "/chainNode", chainNodeInfo);
  }

  public void setDirectoryNodeURI(final String directoryNodeURI) {
    this.directoryNodeURI = directoryNodeURI;
  }
}
