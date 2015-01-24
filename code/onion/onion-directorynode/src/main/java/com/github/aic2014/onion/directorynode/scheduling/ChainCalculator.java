package com.github.aic2014.onion.directorynode.scheduling;

/**
 * Produces a list of indices of chain nodes to be used for a chain.
 */
public interface ChainCalculator {
    public int[] getChainNodeIndices(int totalNodeCount, int chainLength);
}
