package com.github.aic2014.onion.model;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * Information about a routing step, used for debuging/demo purposes.
 */
public class RoutingInfo {
    private UUID chainId;
    private URI requestSender;
    private URI requestRecipient;
    private OnionStatus status;
    private Date latestUpdate;
    private RoutingDirection direction;
    private Message lastInMessage;
    private Message lastOutMessage;



    public RoutingInfo(UUID chainId, URI requestSender, URI requestRecipient, OnionStatus status, RoutingDirection direction, Message lastInMessage, Message lastOutMessage) {
        this.chainId = chainId;
        this.requestSender = requestSender;
        this.requestRecipient = requestRecipient;
        this.status = status;
        this.direction = direction;
        this.latestUpdate = new Date();
        this.lastInMessage = lastInMessage;
        this.lastOutMessage = lastOutMessage;
    }

    public UUID getChainId() {
        return chainId;
    }

    public URI getRequestSender() {
        return requestSender;
    }

    public URI getRequestRecipient() {
        return requestRecipient;
    }

    public OnionStatus getStatus() {
        return status;
    }

    public Date getLatestUpdate() {
        return latestUpdate;
    }

    public RoutingDirection getDirection() {
        return direction;
    }

    public Message getLastInMessage() {
        return lastInMessage;
    }

    public Message getLastOutMessage() {
        return lastOutMessage;
    }

    @Override
    public String toString() {
        return "RoutingInfo{" +
                "chainId=" + chainId +
                ", requestSender=" + requestSender +
                ", requestRecipient=" + requestRecipient +
                ", status=" + status +
                ", latestUpdate=" + latestUpdate +
                ", direction=" + direction +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoutingInfo)) return false;

        RoutingInfo that = (RoutingInfo) o;

        if (chainId != null ? !chainId.equals(that.chainId) : that.chainId != null) return false;
        if (direction != that.direction) return false;
        if (latestUpdate != null ? !latestUpdate.equals(that.latestUpdate) : that.latestUpdate != null) return false;
        if (requestRecipient != null ? !requestRecipient.equals(that.requestRecipient) : that.requestRecipient != null)
            return false;
        if (requestSender != null ? !requestSender.equals(that.requestSender) : that.requestSender != null)
            return false;
        if (status != that.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chainId != null ? chainId.hashCode() : 0;
        result = 31 * result + (requestSender != null ? requestSender.hashCode() : 0);
        result = 31 * result + (requestRecipient != null ? requestRecipient.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (latestUpdate != null ? latestUpdate.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }
}
