package com.github.aic2014.onion.model;

/**
 * Enum for the status of an onion-routed message.
 */
public enum OnionStatus {
    OK,
    //a timeout occurred
    CHAIN_TIMEOUT,
    //an error occurred inside the chain
    CHAIN_ERROR,
    //an error occurred when sending the request to the target
    TARGET_ERROR,
    //an error occurred when sending the request to the target
    TARGET_TIMEOUT,
    TARGET_UNKNOWN_HOST, //an error occurred when sending the request to the target
    TARGET_CONNECTION_REFUSED, TARGET_SSL_HANDSHAKE_FAILED
}
