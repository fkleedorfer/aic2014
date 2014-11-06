package com.github.aic2014.onion.model;

import java.net.URI;
import java.util.UUID;

/**
 * Message class for onion routing.
 */
public class Message {
    UUID id;
    String payload;
    URI sender;
    URI recipient;
    String publicKey;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public URI getSender() {
        return sender;
    }

    public void setSender(URI sender) {
        this.sender = sender;
    }

    public URI getRecipient() {
        return recipient;
    }

    public void setRecipient(URI recipient) {
        this.recipient = recipient;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", payload='" + payload + '\'' +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        if (id != null ? !id.equals(message.id) : message.id != null) return false;
        if (payload != null ? !payload.equals(message.payload) : message.payload != null) return false;
        if (publicKey != null ? !publicKey.equals(message.publicKey) : message.publicKey != null) return false;
        if (recipient != null ? !recipient.equals(message.recipient) : message.recipient != null) return false;
        if (sender != null ? !sender.equals(message.sender) : message.sender != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        return result;
    }
}
