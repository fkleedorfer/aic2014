package com.github.aic2014.onion.directorynode.scheduling;

import java.util.Random;

/**
 * Base class for ChainCalculator implementations.
 */
public abstract class ChainCalculatorBase implements ChainCalculator {
    @Override
    public int[] getChainNodeIndices(int totalNodeCount, int chainLength) {
        Random rnd = new Random(System.currentTimeMillis() + totalNodeCount + chainLength);
        if (totalNodeCount < chainLength) throw new IllegalArgumentException(String.format("Cannot compute chain of length %s from %s nodes", chainLength, totalNodeCount));
        int[] indices = new int[chainLength];
        for (int i = 0; i < chainLength; i++){
            boolean duplicate = false;
            do {
                int newIndex = getRandomIndex(totalNodeCount);
                for (int j = 0; j < i; j++){
                    if (indices[j] == newIndex){
                        duplicate = true;
                        break;
                    }
                }
            } while (duplicate);
        }
        return indices;
    }

    /**
     * Returns the index of a chain node to be used in the chain.
     * @param totalNodeCount
     * @return
     */
    public abstract int getRandomIndex(int totalNodeCount);

}
