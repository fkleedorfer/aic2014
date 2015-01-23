package com.github.aic2014.onion.model;

/**
 * Enum for the status of an onion-routed message.
 */
public enum OnionStatus {
    //ok
    OK,
    //no chain could be obtained from the directory node
    DIRECTORY_ERROR,
    //an error occurred inside the chain
    CHAIN_TIMEOUT,
    //an error occurred when sending the request to the target
    CHAIN_ERROR,
    //an error occurred when sending the request to the target
    TARGET_ERROR,
    //the target timed out
    TARGET_TIMEOUT,
    //the target host is unknown
    TARGET_UNKNOWN_HOST,
    //the target refused the connection
    TARGET_CONNECTION_REFUSED,
    //the target ssl handshake failed
    TARGET_SSL_HANDSHAKE_FAILED
}
