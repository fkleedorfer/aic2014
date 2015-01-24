package com.github.aic2014.onion.directorynode.scheduling;

import java.security.SecureRandom;

/**
 * Chain calculator that uses a uniform distribution.
 */
public class UniformDistributionChainCalculator extends ChainCalculatorBase{

    SecureRandom random = new SecureRandom(Long.toHexString(System.currentTimeMillis()).getBytes());

    public int getRandomIndex(int totalNodeCount) {
        return random.nextInt(totalNodeCount);
    }

}
