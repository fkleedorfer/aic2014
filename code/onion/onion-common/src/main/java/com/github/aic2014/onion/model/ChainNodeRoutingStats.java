package com.github.aic2014.onion.model;

/**
 * Model for simple statistics collected by a chain node about its operation.
 */
public class ChainNodeRoutingStats {
  //length of the time window that the statistics were collected in
  private long timeWindowSize;
  //number of messages processed (correctly or with error) within the timeframe
  private int messagesProcessed;
  //number of messages that were received and for which the response has not yet been returned
  private int messagesPending;
  //number of errors#
  private int errors;
  //number of milliseconds since the last message was received
  private long millisSinceLastMessageIn;
  //number of milliseconds since the last message was returned
  private long millisSinceLastMessageProcessed;
  private long timeSpentInFailedRequests;
  private long timeSpentInSuccessfulRequests;

  public long getTimeWindowSize() {
    return timeWindowSize;
  }

  public void setTimeWindowSize(long timeWindowSize) {
    this.timeWindowSize = timeWindowSize;
  }

  public int getMessagesProcessed() {
    return messagesProcessed;
  }

  public void setMessagesProcessed(int messagesProcessed) {
    this.messagesProcessed = messagesProcessed;
  }

  public int getMessagesPending() {
    return messagesPending;
  }

  public void setMessagesPending(int messagesPending) {
    this.messagesPending = messagesPending;
  }

  public int getErrors() {
    return errors;
  }

  public void setErrors(int errors) {
    this.errors = errors;
  }

  public long getMillisSinceLastMessageIn() {
    return millisSinceLastMessageIn;
  }

  public void setMillisSinceLastMessageIn(long millisSinceLastMessageIn) {
    this.millisSinceLastMessageIn = millisSinceLastMessageIn;
  }

  public long getMillisSinceLastMessageProcessed() {
    return millisSinceLastMessageProcessed;
  }

  public void setMillisSinceLastMessageProcessed(long millisSinceLastMessageProcessed) {
    this.millisSinceLastMessageProcessed = millisSinceLastMessageProcessed;
  }

  public void setTimeSpentInFailedRequests(long timeSpentInFailedRequests) {
    this.timeSpentInFailedRequests = timeSpentInFailedRequests;
  }

  public long getTimeSpentInFailedRequests() {
    return timeSpentInFailedRequests;
  }

  public void setTimeSpentInSuccessfulRequests(long timeSpentInSuccessfulRequests) {
    this.timeSpentInSuccessfulRequests = timeSpentInSuccessfulRequests;
  }

  public long getTimeSpentInSuccessfulRequests() {
    return timeSpentInSuccessfulRequests;
  }

  @Override
  public String toString() {
    return "ChainNodeRoutingStats{" +
            "timeWindowSize=" + timeWindowSize +
            ", messagesProcessed=" + messagesProcessed +
            ", messagesPending=" + messagesPending +
            ", errors=" + errors +
            ", millisSinceLastMessageIn=" + millisSinceLastMessageIn +
            ", millisSinceLastMessageProcessed=" + millisSinceLastMessageProcessed +
            ", timeSpentInFailedRequests=" + timeSpentInFailedRequests +
            ", timeSpentInSuccessfulRequests=" + timeSpentInSuccessfulRequests +
            '}';
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) return true;
    if (!(o instanceof ChainNodeRoutingStats)) return false;

    ChainNodeRoutingStats that = (ChainNodeRoutingStats) o;

    if (errors != that.errors) return false;
    if (messagesPending != that.messagesPending) return false;
    if (messagesProcessed != that.messagesProcessed) return false;
    if (millisSinceLastMessageIn != that.millisSinceLastMessageIn) return false;
    if (millisSinceLastMessageProcessed != that.millisSinceLastMessageProcessed) return false;
    if (timeSpentInFailedRequests != that.timeSpentInFailedRequests) return false;
    if (timeSpentInSuccessfulRequests != that.timeSpentInSuccessfulRequests) return false;
    if (timeWindowSize != that.timeWindowSize) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (int) (timeWindowSize ^ (timeWindowSize >>> 32));
    result = 31 * result + messagesProcessed;
    result = 31 * result + messagesPending;
    result = 31 * result + errors;
    result = 31 * result + (int) (millisSinceLastMessageIn ^ (millisSinceLastMessageIn >>> 32));
    result = 31 * result + (int) (millisSinceLastMessageProcessed ^ (millisSinceLastMessageProcessed >>> 32));
    result = 31 * result + (int) (timeSpentInFailedRequests ^ (timeSpentInFailedRequests >>> 32));
    result = 31 * result + (int) (timeSpentInSuccessfulRequests ^ (timeSpentInSuccessfulRequests >>> 32));
    return result;
  }
}
