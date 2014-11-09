package com.github.aic2014.onion.chainnode;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple thread-safe in-memory implementation.
 */
public class InMemoryResponseInfoService implements ResponseInfoService {
  private ConcurrentHashMap<UUID, ResponseInfo> responseInfoMap = new ConcurrentHashMap<UUID, ResponseInfo>();
  @Override
  public void addResponseInfo(UUID id, ResponseInfo responseInfo) {
    responseInfoMap.put(id, responseInfo);
  }

  @Override
  public ResponseInfo getAndDeleteResponseInfo(UUID id) {
    return responseInfoMap.remove(id);
  }
}

