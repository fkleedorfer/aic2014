package com.github.aic2014.onion.chainnode;

import com.github.aic2014.onion.model.ChainNodeRoutingStats;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects the statistics for the chain node's operation.
 * The statistics are reset when the getChainNodeRoutingStats method is
 * called and more than resetAfterMillis milliseconds have passed since
 * the collectionStartTimeStamp.
 */
public class ChainNodeStatsCollector {
    public static final long DEFAULT_RESET_TIMEOUT = 30000;
    //timestamp when we started collecting data
    private AtomicLong collectionStartTimeStamp = new AtomicLong(System.currentTimeMillis());
    //number of messages processed (correctly or with error) within the timeframe
    private AtomicInteger messagesProcessed = new AtomicInteger();
    //number of messages that were received and for which the response has not yet been returned
    private AtomicInteger messagesPending = new AtomicInteger();
    //number of processing errors
    private AtomicInteger errors = new AtomicInteger();
    //number of milliseconds since the last message was received
    private AtomicLong lastMessageReceivedTimestamp = new AtomicLong();
    //number of milliseconds since the last message was returned
    private AtomicLong lastMessageProcessedTimestamp = new AtomicLong();
    //milliseconds spent processing successful requests
    private AtomicLong timeSpentInSuccessfulRequests = new AtomicLong();
    //milliseconds spent processing failed requests
    private AtomicLong timeSpentInFailedRequests = new AtomicLong();

    private long resetAfterMillis = DEFAULT_RESET_TIMEOUT;

    public ChainNodeStatsCollector() {
    }

    public ChainNodeStatsCollector(long resetAfterMillis) {
        this.resetAfterMillis = resetAfterMillis;
    }

    private void reset(){
        this.collectionStartTimeStamp.set(System.currentTimeMillis());
        this.messagesProcessed.set(0);
        this.errors.set(0);
        this.lastMessageReceivedTimestamp.set(-1);
        this.lastMessageProcessedTimestamp.set(-1);
        this.timeSpentInFailedRequests.set(0);
        this.timeSpentInSuccessfulRequests.set(0);
    }

    private void resetIfNecessary(){
        if (System.currentTimeMillis() - this.collectionStartTimeStamp.get() > this.resetAfterMillis){
            reset();
        }
    }

    public void onMessageReceived(){
        this.messagesPending.getAndIncrement();
        this.lastMessageReceivedTimestamp.set(System.currentTimeMillis());
    }

    public void onMessageProcessingError(long timeSpent){
        this.errors.getAndIncrement();
        this.timeSpentInFailedRequests.addAndGet(timeSpent);
    }

    public void onMessageProcessed(long timeSpent){
        this.messagesPending.getAndDecrement();
        this.messagesProcessed.getAndIncrement();
        this.lastMessageProcessedTimestamp.set(System.currentTimeMillis());
        this.timeSpentInSuccessfulRequests.addAndGet(timeSpent);
    }

    public int getMessagesPending(){
      return this.messagesPending.get();
    }

    public ChainNodeRoutingStats getChainNodeRoutingStats(){
        ChainNodeRoutingStats stats = new ChainNodeRoutingStats();
        stats.setTimeWindowSize(diffToNow(this.collectionStartTimeStamp.get()));
        stats.setMessagesPending(this.messagesPending.get());
        stats.setMessagesProcessed(this.messagesProcessed.get());
        stats.setErrors(this.errors.get());
        stats.setMillisSinceLastMessageIn(diffToNow(this.lastMessageReceivedTimestamp.get()));
        stats.setMillisSinceLastMessageProcessed(diffToNow(this.lastMessageProcessedTimestamp.get()));
        stats.setTimeSpentInSuccessfulRequests(this.timeSpentInSuccessfulRequests.get());
        stats.setTimeSpentInFailedRequests(this.timeSpentInFailedRequests.get());
        resetIfNecessary();
        return stats;
    }

    private long diffToNow(long then) {
        if (then == -1) return -1;
        return System.currentTimeMillis() - then;
    }
}
