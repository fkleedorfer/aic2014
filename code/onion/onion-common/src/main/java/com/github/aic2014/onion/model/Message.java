package com.github.aic2014.onion.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.net.URI;
import java.util.UUID;

/**
 * Message class for onion routing.
 */
@JsonIgnoreProperties(ignoreUnknown = true) //when deserializing, do not complain about unknown values
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL) //do not serialize nulls.
public class Message {
    //The chainId of the message, also the chainId of the circuit.
    UUID chainId;
    //The forwarded payload of the message.
    String payload;
    //The sender of the message.
    URI sender;
    //The recipient of the message.
    URI recipient;
    //The public key to use for encrypting the response.
    String publicKey;
    //The number of hops to go - 0 means the payload is the actual http request
    int hopsToGo;
    //status code (one of the status constants)
    OnionStatus status = OnionStatus.OK;
    //an optional error message (use only if status != OnionStatus.OK)
    String errorMessage;
    //if the routing failed due to a node that misbehaved (timed out, returned error)
    //its URI is passed here.
    URI misbehavingNode;
    //information for debugging
    String debugInfo;

  /**
   * Constructor that sets the debuginfo.
   * @param debugInfo
   */
  public Message(String debugInfo) {
    this.debugInfo = debugInfo;
  }

  public Message() {
  }

  public String getDebugInfo() {
    return debugInfo;
  }

  public void setDebugInfo(String debugInfo) {
    this.debugInfo = debugInfo;
  }

  public UUID getChainId() {
        return chainId;
    }

    public void setChainId(UUID chainId) {
        this.chainId = chainId;
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

    public int getHopsToGo() {
        return hopsToGo;
    }

    public void setHopsToGo(int hopsToGo) {
        this.hopsToGo = hopsToGo;
    }

    public OnionStatus getStatus() {
        return status;
    }

    public void setStatus(OnionStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public URI getMisbehavingNode() {
        return misbehavingNode;
    }

    public void setMisbehavingNode(URI misbehavingNode) {
        this.misbehavingNode = misbehavingNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        if (hopsToGo != message.hopsToGo) return false;
        if (chainId != null ? !chainId.equals(message.chainId) : message.chainId != null) return false;
        if (errorMessage != null ? !errorMessage.equals(message.errorMessage) : message.errorMessage != null)
            return false;
        if (misbehavingNode != null ? !misbehavingNode.equals(message.misbehavingNode) : message.misbehavingNode != null)
            return false;
        if (payload != null ? !payload.equals(message.payload) : message.payload != null) return false;
        if (publicKey != null ? !publicKey.equals(message.publicKey) : message.publicKey != null) return false;
        if (recipient != null ? !recipient.equals(message.recipient) : message.recipient != null) return false;
        if (sender != null ? !sender.equals(message.sender) : message.sender != null) return false;
        if (status != message.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chainId != null ? chainId.hashCode() : 0;
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        result = 31 * result + hopsToGo;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + (misbehavingNode != null ? misbehavingNode.hashCode() : 0);
        return result;
    }

  private String abbreviate(String original, int length) {
    if (original == null) return "null";
    if (original.length() <= length) return original;
    return original.substring(0,length) + "...";
  }

  @Override
  public String toString() {
    return "Message{" +
            "chainId=" + abbreviate(chainId.toString(),20) +
            ", payload='" + abbreviate(payload,20) +'\'' +
            ", sender=" + sender +
            ", recipient=" + recipient +
            ", publicKey='" + abbreviate(publicKey,20) + '\'' +
            ", hopsToGo=" + hopsToGo +
            ", status=" + status +
            ", errorMessage='" + errorMessage + '\'' +
            ", misbehavingNode=" + misbehavingNode +
            ", debugInfo='" + debugInfo + '\'' +
            '}';
  }
}
