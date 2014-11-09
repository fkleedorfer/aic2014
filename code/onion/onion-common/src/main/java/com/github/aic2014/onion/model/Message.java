package com.github.aic2014.onion.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.net.URI;
import java.util.UUID;

/**
 * Message class for onion routing.
 */
@JsonIgnoreProperties(ignoreUnknown = true) //when deserializing, do not complain about unknown values
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) //do not serialize nulls
public class Message {
  //The id of the message, also the id of the circuit.
  UUID id;
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

  public int getHopsToGo() {
    return hopsToGo;
  }

  public void setHopsToGo(int hopsToGo) {
    this.hopsToGo = hopsToGo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Message)) return false;

    Message message = (Message) o;

    if (hopsToGo != message.hopsToGo) return false;
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
    result = 31 * result + hopsToGo;
    return result;
  }

  @Override
  public String toString() {
    return "Message{" +
            "id=" + id +
            ", payload='" + payload + '\'' +
            ", sender=" + sender +
            ", recipient=" + recipient +
            ", publicKey='" + publicKey + '\'' +
            ", hopsToGo=" + hopsToGo +
            '}';
  }
}
