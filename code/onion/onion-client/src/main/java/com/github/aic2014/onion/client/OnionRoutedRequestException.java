package com.github.aic2014.onion.client;

import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.OnionStatus;

public class OnionRoutedRequestException extends Exception {

    private ChainNodeInfo failedNode;
    private OnionStatus status;

    public OnionRoutedRequestException() {
        super();
    }

    public OnionRoutedRequestException(OnionStatus status) {
        super("Onion Routing error: " + status.toString());
        setStatus(status);
    }

    public OnionRoutedRequestException(OnionStatus status, Throwable throwable) {
        super("Onion Routing error: " + status.toString(), throwable);
        setStatus(status);
    }

    public OnionRoutedRequestException(String s) {
        super(s);
    }

    public OnionRoutedRequestException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public OnionRoutedRequestException(Throwable throwable) {
        super(throwable);
    }

    public ChainNodeInfo getFailedNode() {
        return failedNode;
    }

    public void setFailedNode(ChainNodeInfo failedNode) {
        this.failedNode = failedNode;
    }

    public OnionStatus getStatus() {
        return status;
    }

    public void setStatus(OnionStatus status) {
        this.status = status;
    }
}
