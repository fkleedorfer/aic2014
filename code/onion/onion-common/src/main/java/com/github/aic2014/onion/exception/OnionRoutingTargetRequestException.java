package com.github.aic2014.onion.exception;

import com.github.aic2014.onion.model.OnionStatus;

/**
 * Indicates that something went wrong when sending the
 * onion-routed request to the target.
 */
public class OnionRoutingTargetRequestException extends OnionRoutingException {
    OnionStatus responseStatus;

    public OnionRoutingTargetRequestException(OnionStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public OnionRoutingTargetRequestException(String message, OnionStatus responseStatus) {
        super(message);
        this.responseStatus = responseStatus;
    }

    public OnionRoutingTargetRequestException(String message, Throwable cause, OnionStatus responseStatus) {
        super(message, cause);
        this.responseStatus = responseStatus;
    }

    public OnionRoutingTargetRequestException(Throwable cause, OnionStatus responseStatus) {
        super(cause);
        this.responseStatus = responseStatus;
    }

    public OnionRoutingTargetRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, OnionStatus responseStatus) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.responseStatus = responseStatus;
    }

    public OnionStatus getResponseStatus() {
        return responseStatus;
    }
}
