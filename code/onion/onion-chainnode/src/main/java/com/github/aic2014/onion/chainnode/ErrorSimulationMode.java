package com.github.aic2014.onion.chainnode;

/**
 * The chain node can be configured to simulate erroneous behaviour. This
 * class enumerates the possible modes.
 */
public enum ErrorSimulationMode {
  NO_ERROR,
  RETURN_404,
  SLOW_ACCEPT,
  SLOW_RESPONSE
}
