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
    //number of milliseconds since the last message was received
    private long millisSinceLastMessageIn;
    //number of milliseconds since the last message was returned
    private long millisSinceLastMessageProcessed;

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

    @Override
    public String toString() {
        return "ChainNodeRoutingStats{" +
                "timeWindowSize=" + timeWindowSize +
                ", messagesProcessed=" + messagesProcessed +
                ", messagesPending=" + messagesPending +
                ", millisSinceLastMessageIn=" + millisSinceLastMessageIn +
                ", millisSinceLastMessageProcessed=" + millisSinceLastMessageProcessed +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof ChainNodeRoutingStats)) return false;

        ChainNodeRoutingStats that = (ChainNodeRoutingStats) o;

        if (messagesPending != that.messagesPending) return false;
        if (messagesProcessed != that.messagesProcessed) return false;
        if (millisSinceLastMessageIn != that.millisSinceLastMessageIn) return false;
        if (millisSinceLastMessageProcessed != that.millisSinceLastMessageProcessed) return false;
        if (timeWindowSize != that.timeWindowSize) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (timeWindowSize ^ (timeWindowSize >>> 32));
        result = 31 * result + messagesProcessed;
        result = 31 * result + messagesPending;
        result = 31 * result + (int) (millisSinceLastMessageIn ^ (millisSinceLastMessageIn >>> 32));
        result = 31 * result + (int) (millisSinceLastMessageProcessed ^ (millisSinceLastMessageProcessed >>> 32));
        return result;
    }
}
