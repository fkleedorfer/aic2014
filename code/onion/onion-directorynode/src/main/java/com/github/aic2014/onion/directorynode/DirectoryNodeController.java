package com.github.aic2014.onion.directorynode;


import com.github.aic2014.onion.model.ChainNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Collection;
import java.util.List;


/**
 * Controller that manages a REST-Style collection of ChainNodeInfo objects and also
 * produces a random chain.
 */
@Controller
public class DirectoryNodeController
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private ChainNodeService chainNodeService;


  /**
   * Registers a chain node info and assigns an id for it
   * The id is returned in the value of the HTTP 'Location' header.
   *
   * @param request
   * @param chainNodeInfo
   * @return
   */
  @RequestMapping(
    value = "/chainNode",
    method = RequestMethod.POST
  )
  public ResponseEntity<Object> registerChainNode(HttpServletRequest request,
    @RequestBody ChainNodeInfo chainNodeInfo) {
    int id = this.chainNodeService.registerChainNode(chainNodeInfo);
    URI newUri = createChainNodeUri(request, id);
    logger.info("chain node {} registered, assigned uri {}", chainNodeInfo, newUri);
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(newUri);
    return new ResponseEntity<Object>(headers, HttpStatus.CREATED);
  }



  /**
   * Returns a collection of all registered chain nodes.
   *
   * @return
   */
  @RequestMapping(
    value = "/chainNode",
    method = RequestMethod.GET
  )
  public ResponseEntity<Collection<ChainNodeInfo>> getAllChainNodes() {
    return new ResponseEntity<Collection<ChainNodeInfo>>(this.chainNodeService.getAllChainNodes(), HttpStatus.OK);
  }

  /**
   * Returns the chain node with the specified id.
   *
   * @param id
   * @return
   */
  @RequestMapping(
    value = "/chainNode/{id}",
    method = RequestMethod.GET
  )
  public ResponseEntity<ChainNodeInfo> getChainNode(@PathVariable @NotNull Integer id) {
    return new ResponseEntity<ChainNodeInfo>(this.chainNodeService.getChainNode(id), HttpStatus.OK);
  }

  /**
   * Unregisters the chain node with the specified id.
   * @param id
   * @return
   */
  @RequestMapping(
    value = "/chainNode/{id}",
    method = RequestMethod.DELETE
  )
  public ResponseEntity<Object> deleteChainNode(HttpServletRequest request, @PathVariable @NotNull Integer id) {
    this.chainNodeService.unregisterChainNode(id);
    logger.info("chain node {} unregistered", request.getRequestURL());
    return new ResponseEntity<Object>(HttpStatus.OK);
  }

  /**
   * Returns a random chain of chain nodes.
   * @return
   */
  @RequestMapping(
    value = "/getChain",
    method = RequestMethod.GET
  )
  public ResponseEntity<List<ChainNodeInfo>> getChain (){
    return new ResponseEntity<List<ChainNodeInfo>>(this.chainNodeService.getChain(), HttpStatus.OK);
  }

  private URI createChainNodeUri(final HttpServletRequest request, final int id) {
    return URI.create(request.getRequestURL().toString() + "/"+id);
  }
}


