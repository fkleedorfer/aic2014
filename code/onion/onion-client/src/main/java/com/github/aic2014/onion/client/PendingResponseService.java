package com.github.aic2014.onion.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps track of responses expected to be tunneled back through
 * the chain.
 */
public class PendingResponseService {
  private Map<UUID, PendingResponse> pendingResponses = Collections.synchronizedMap(new HashMap<UUID, PendingResponse>());
  public void addPendingResponse(UUID chainId, PendingResponse pendingResponse){
    this.pendingResponses.put(chainId, pendingResponse);
  }

  public PendingResponse getAndRemovePendingResponse(UUID chainId) {
    return this.pendingResponses.get(chainId);
  }
}
