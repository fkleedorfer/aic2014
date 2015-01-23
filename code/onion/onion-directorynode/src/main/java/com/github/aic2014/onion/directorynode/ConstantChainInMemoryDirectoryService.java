package com.github.aic2014.onion.directorynode;

import com.github.aic2014.onion.model.ChainNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service that always returns the same chain. Only for debugging.
 */
public class ConstantChainInMemoryDirectoryService implements DirectoryNodeService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int CHAIN_LENGTH = 3;
    private List<ChainNodeInfo> chainNodeInfos = Collections.synchronizedList(new ArrayList<ChainNodeInfo>());

    @Override
    public String registerChainNode(final ChainNodeInfo chainNodeInfo) {
        if (chainNodeInfo == null)
            return "null";

        chainNodeInfos.add(chainNodeInfo);
        chainNodeInfo.setId(UUID.randomUUID().toString());
        return chainNodeInfo.getId();
    }

    @Override
    public void unregisterChainNode(String id) {
        assert id == null : "id must be non-null";
        this.chainNodeInfos.remove(findChainNodeInfo(id));
    }

    private ChainNodeInfo findChainNodeInfo(String id) {
        Optional<ChainNodeInfo> result = chainNodeInfos.stream().filter(cni -> cni.getId().equals(id)).findAny();
        return result.isPresent() ? result.get() : null;
    }

    @Override
    public ChainNodeInfo getChainNode(final String id) {
        assert id == null : "id must be non-null";
        return findChainNodeInfo(id);
    }

    @Override
    public Collection<ChainNodeInfo> getAllChainNodes() {
        List<ChainNodeInfo> copy = new ArrayList<ChainNodeInfo>(this.chainNodeInfos.size());
        copy.addAll(this.chainNodeInfos);
        return copy;
    }

    @Override
    public List<ChainNodeInfo> getChain() {
        List<ChainNodeInfo> chain = new ArrayList<ChainNodeInfo>(CHAIN_LENGTH);
        int i = 0;
        for(ChainNodeInfo info: this.chainNodeInfos){
          chain.add(info);
          if (++i >= CHAIN_LENGTH) break;
        }
        return chain;
    }
}
