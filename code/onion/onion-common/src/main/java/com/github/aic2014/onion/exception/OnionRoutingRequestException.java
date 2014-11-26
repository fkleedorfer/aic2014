package com.github.aic2014.onion.exception;

import java.net.URI;

/**
 * Exception to be used when sending a message from one chain node
 * to the next or to the destination fails.
 * Used to encapsulate information about which nodes failed.
 */
public class OnionRoutingRequestException extends OnionRoutingException {
    private URI misbehavingNode;

    public OnionRoutingRequestException(URI misbehavingNode) {
        this.misbehavingNode = misbehavingNode;
    }

    public OnionRoutingRequestException(String message, URI misbehavingNode) {
        super(message);
        this.misbehavingNode = misbehavingNode;
    }

    public OnionRoutingRequestException(String message, Throwable cause, URI misbehavingNode) {
        super(message, cause);
        this.misbehavingNode = misbehavingNode;
    }

    public OnionRoutingRequestException(Throwable cause, URI misbehavingNode) {
        super(cause);
        this.misbehavingNode = misbehavingNode;
    }

    public OnionRoutingRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, URI misbehavingNode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.misbehavingNode = misbehavingNode;
    }

    public URI getMisbehavingNode() {
        return misbehavingNode;
    }

    @Override
    public String toString() {
        return "OnionRoutingRequestException{" +
                "misbehavingNode=" + misbehavingNode +
                '}';
    }


}
