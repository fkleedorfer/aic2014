package com.github.aic2014.onion.directorynode;

import com.github.aic2014.onion.model.ChainNodeInfo;

import java.util.*;

public class InMemoryChainNodeService implements ChainNodeService
{
  private static final int CHAIN_LENGTH = 3;
  private List<ChainNodeInfo> chainNodeInfos = Collections.synchronizedList(new ArrayList<ChainNodeInfo>());

  @Override
  public Integer registerChainNode(final ChainNodeInfo chainNodeInfo) {
    int index = chainNodeInfos.indexOf(chainNodeInfo);
    if (index > -1) {
      return index;
    }
    this.chainNodeInfos.add(chainNodeInfo);
    return this.chainNodeInfos.indexOf(chainNodeInfo);
  }

  @Override
  public void unregisterChainNode(Integer id) {
    assert id == null || id < 0 : "id must be non-null and >= 0";
    this.chainNodeInfos.remove(id);
  }

  @Override
  public ChainNodeInfo getChainNode(final Integer id) {
    assert id == null || id < 0 : "id must be non-null and >= 0";
    return this.chainNodeInfos.get(id);
  }

  @Override
  public Collection<ChainNodeInfo> getAllChainNodes() {
    List<ChainNodeInfo> copy = new ArrayList<ChainNodeInfo>(this.chainNodeInfos.size());
    copy.addAll(this.chainNodeInfos);
    return copy;
  }

  @Override
  public List<ChainNodeInfo> getChain() {
    List<ChainNodeInfo> chain = new ArrayList<ChainNodeInfo>(3);
    if (this.chainNodeInfos.size() < CHAIN_LENGTH) {
      throw new IllegalStateException("At least " + CHAIN_LENGTH +
        " chain nodes must be registered to build a chain. " +
        "Currently registered: " + this.chainNodeInfos.size());
    }
    Collections.shuffle(this.chainNodeInfos);
    Iterator<ChainNodeInfo> it = this.chainNodeInfos.iterator();
    for (int i = 0; i < CHAIN_LENGTH; i++){
      chain.add(it.next());
    }
    return chain;
  }
}
