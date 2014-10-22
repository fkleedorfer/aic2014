package com.github.aic2014.onion.directorynode;

import com.github.aic2014.onion.model.ChainNodeInfo;

import java.util.Collection;
import java.util.List;

/**
 * Registry for chain nodes.
 */
public interface ChainNodeService
{
  /**
   * Registers a chain node. Returns the internal id assigned to it.
   *
   * @param chainNodeInfo
   * @return
   */
  public Integer registerChainNode(ChainNodeInfo chainNodeInfo);

  /**
   * Unregisters a chain node - i.e., deletes it from the list of chain
   * nodes maintained by this directory node.
   * The id must be non-null >= 0.
   * @param id
   */
  public void unregisterChainNode(Integer id);

  /**
   * Returns the chain node with the specified id.
   * The id must be non-null >= 0.
   * @param id
   */
  public ChainNodeInfo getChainNode(Integer id);

  /**
   * Returns a list of all chain nodes.
   * @return
   */
  public Collection<ChainNodeInfo> getAllChainNodes();

  /**
   * Returns a list of 3 randomly chosen chain nodes.
   * @return
   */
  public List<ChainNodeInfo> getChain();

}
