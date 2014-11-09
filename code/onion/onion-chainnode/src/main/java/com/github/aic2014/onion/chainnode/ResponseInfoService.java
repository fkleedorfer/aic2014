package com.github.aic2014.onion.chainnode;

import java.util.UUID;

/**
 * Manages ResponseInfo objects.
 * Supports adding and removing objects.
 */
public interface ResponseInfoService {
  public void addResponseInfo(UUID id, ResponseInfo responseInfo);
  public ResponseInfo getAndDeleteResponseInfo(UUID id);
}
