package com.github.aic2014.onion.model;

import java.net.URI;

/**
 * Model class for communicating chain node information
 * between chain node and directory node via REST+JSON
 */
public class ChainNodeInfo
{
  private URI uri;
  //TODO: add information about the public key


  public URI getUri() {
    return uri;
  }

  public void setUri(final URI uri) {
    this.uri = uri;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof ChainNodeInfo)) return false;

    final ChainNodeInfo that = (ChainNodeInfo) o;

    if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return uri != null ? uri.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "ChainNodeInfo{" +
      "uri=" + uri +
      '}';
  }
}
