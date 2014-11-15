package com.github.aic2014.onion.directorynode;

import com.github.aic2014.onion.model.ChainNodeInfo;

import java.util.Collection;
import java.util.List;

/**
 * Registry for chain nodes.
 */
public interface DirectoryNodeService {
    /**
     * Registers a chain node. Returns the internal id assigned to it.
     *
     * @param chainNodeInfo
     * @return
     */
    public String registerChainNode(ChainNodeInfo chainNodeInfo);

    /**
     * Unregisters a chain node - i.e., deletes it from the list of chain
     * nodes maintained by this directory node.
     * The id must be non-null. (in case of AWS, it (most likely) represents the AWS-Instance-ID)
     *
     * @param id
     */
    public void unregisterChainNode(String id);

    /**
     * Returns the chain node with the specified id.
     * The id must be non-null. (in case of AWS, it (most likely) represents the AWS-Instance-ID)
     *
     * @param id
     */
    public ChainNodeInfo getChainNode(String id);

    /**
     * Returns a list of all chain nodes.
     *
     * @return
     */
    public Collection<ChainNodeInfo> getAllChainNodes();

    /**
     * Returns a list of 3 randomly chosen chain nodes.
     *
     * @return
     */
    public List<ChainNodeInfo> getChain();

}
