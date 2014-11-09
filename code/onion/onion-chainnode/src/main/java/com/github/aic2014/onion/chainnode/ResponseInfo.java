package com.github.aic2014.onion.chainnode;

import java.net.URI;

/**
 * Model holding all the information a chain node needs
 * to send a response back the path the request was
 * routed: the URI of the original sender and the public
 * key for encrypting the payload.
 */
public class ResponseInfo {
  URI senderOfRequest;
  String publicKey;

  public ResponseInfo() {
  }

  public ResponseInfo(URI senderOfRequest, String publicKey) {
    this.senderOfRequest = senderOfRequest;
    this.publicKey = publicKey;
  }

  public URI getSenderOfRequest() {
    return senderOfRequest;
  }

  public void setSenderOfRequest(URI senderOfRequest) {
    this.senderOfRequest = senderOfRequest;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public String toString() {
    return "ResponseInfo{" +
            "senderOfRequest=" + senderOfRequest +
            ", publicKey='" + publicKey + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ResponseInfo)) return false;

    ResponseInfo that = (ResponseInfo) o;

    if (publicKey != null ? !publicKey.equals(that.publicKey) : that.publicKey != null) return false;
    if (senderOfRequest != null ? !senderOfRequest.equals(that.senderOfRequest) : that.senderOfRequest != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = senderOfRequest != null ? senderOfRequest.hashCode() : 0;
    result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
    return result;
  }
}
