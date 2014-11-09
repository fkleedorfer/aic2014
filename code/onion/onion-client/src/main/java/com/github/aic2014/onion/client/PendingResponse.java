package com.github.aic2014.onion.client;

import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.UUID;

/**
 * Encapsulates all information about an onion-routed request on the
 * originator's side that is necessary to process the response when
 * it arrives.
 * <p>
 * When the response arrives, the corresponding PendingResponse object
 * is retrieved and its response member is set, propagating the
 * response to clients potentially waiting on the response.get()
 * method call.
 */
public class PendingResponse {
  private UUID chainId;
  private SettableListenableFuture<String> response;
  private int chainLength;

  public PendingResponse(UUID chainId, SettableListenableFuture<String> futureResponse, int chainLength) {
    this.chainId = chainId;
    this.response = futureResponse;
    this.chainLength = chainLength;
  }

  public int getChainLength() {
    return chainLength;
  }

  public void setChainLength(int chainLength) {
    this.chainLength = chainLength;
  }

  public UUID getChainId() {
    return chainId;
  }

  public void setChainId(UUID chainId) {
    this.chainId = chainId;
  }

  public SettableListenableFuture<String> getResponse() {
    return response;
  }

  public void setResponse(SettableListenableFuture<String> response) {
    this.response = response;
  }

  @Override
  public String toString() {
    return "PendingResponse{" +
            "chainId=" + chainId +
            ", response=" + response +
            ", chainLength=" + chainLength +
            '}';
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) return true;
    if (!(o instanceof PendingResponse)) return false;

    PendingResponse that = (PendingResponse) o;

    if (chainLength != that.chainLength) return false;
    if (chainId != null ? !chainId.equals(that.chainId) : that.chainId != null) return false;
    if (response != null ? !response.equals(that.response) : that.response != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = chainId != null ? chainId.hashCode() : 0;
    result = 31 * result + (response != null ? response.hashCode() : 0);
    result = 31 * result + chainLength;
    return result;
  }
}
