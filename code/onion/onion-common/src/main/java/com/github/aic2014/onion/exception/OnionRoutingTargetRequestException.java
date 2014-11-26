package com.github.aic2014.onion.exception;

/**
 * Indicates that something went wrong when sending the
 * onion-routed request to the target.
 */
public class OnionRoutingTargetRequestException extends OnionRoutingException {
    public OnionRoutingTargetRequestException() {
    }

    public OnionRoutingTargetRequestException(String message) {
        super(message);
    }

    public OnionRoutingTargetRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public OnionRoutingTargetRequestException(Throwable cause) {
        super(cause);
    }

    public OnionRoutingTargetRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
