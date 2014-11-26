package com.github.aic2014.onion.model;

/**
 * Enum for the status of an onion-routed message.
 */
public enum OnionStatus {
    OK,
    //a timeout occurred
    CHAIN_TIMEOUT,
    //an error occurred inside the chain
    ERROR,
    //an error occurred when sending the request to the target
    TARGET_ERROR
}
