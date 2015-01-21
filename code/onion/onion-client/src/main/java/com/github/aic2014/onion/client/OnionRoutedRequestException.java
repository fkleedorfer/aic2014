package com.github.aic2014.onion.client;

import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.OnionStatus;

import java.util.Optional;

/**
 * Thrown in case an Onion Routing request does not succeed.
 */
public class OnionRoutedRequestException extends Exception {

    private ChainNodeInfo misbehavingNode;
    private OnionStatus status;

    public OnionRoutedRequestException() {
        super();
    }

    public OnionRoutedRequestException(OnionStatus status) {
        this(status, Optional.empty());
    }

    public OnionRoutedRequestException(OnionStatus status, Throwable throwable) {
        this(status, Optional.empty(), throwable);
    }

    public OnionRoutedRequestException(OnionStatus status, Optional<ChainNodeInfo> misbehavingNode) {
        super(buildMessage(status, misbehavingNode));
        setStatus(status);
        misbehavingNode.ifPresent(this::setMisbehavingNode);
    }

    public OnionRoutedRequestException(OnionStatus status, Optional<ChainNodeInfo> misbehavingNode, Throwable throwable) {
        super(buildMessage(status, misbehavingNode), throwable);
        setStatus(status);
        misbehavingNode.ifPresent(this::setMisbehavingNode);
    }

    private static String buildMessage(OnionStatus status, Optional<ChainNodeInfo> misbehavingNode) {
        String msg = String.format("Onion Routing %s", status);
        if (misbehavingNode.isPresent())
            msg += String.format(" (Culprit: %s)", misbehavingNode.get());
        return msg;
    }

    public ChainNodeInfo getMisbehavingNode() {
        return misbehavingNode;
    }

    public void setMisbehavingNode(ChainNodeInfo misbehavingNode) {
        this.misbehavingNode = misbehavingNode;
    }

    public OnionStatus getStatus() {
        return status;
    }

    public void setStatus(OnionStatus status) {
        this.status = status;
    }
}
