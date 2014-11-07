package com.github.aic2014.onion.exception;


public class OnionRoutingException extends RuntimeException {
  public OnionRoutingException() {
  }

  public OnionRoutingException(String message) {
    super(message);
  }

  public OnionRoutingException(String message, Throwable cause) {
    super(message, cause);
  }

  public OnionRoutingException(Throwable cause) {
    super(cause);
  }

  public OnionRoutingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
