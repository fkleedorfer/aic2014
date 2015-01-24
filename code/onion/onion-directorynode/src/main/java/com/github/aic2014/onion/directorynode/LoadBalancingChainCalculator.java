package com.github.aic2014.onion.directorynode;

import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.ChainNodeRoutingStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chain calculator that tries to do load balancing based on
 * ChainNodeStats.
 */
public class LoadBalancingChainCalculator  {
    private ConcurrentHashMap<String, StatsInfoHolder> idsTostats = new ConcurrentHashMap<>();

    public void registerChainNode(ChainNodeInfo chainNodeInfo){
        this.idsTostats.put(chainNodeInfo.getId(), new StatsInfoHolder(null, chainNodeInfo));
    }

    public void updateStats(ChainNodeRoutingStats stats , ChainNodeInfo chainNodeInfo){
        this.idsTostats.put(chainNodeInfo.getId(), new StatsInfoHolder(stats, chainNodeInfo));
    }

    public ChainNodeInfo[] getChain(int length) {
        if (length > this.idsTostats.size()) throw new IllegalArgumentException(String.format("Cannot compute chain of length %s from %s nodes", length, this.idsTostats.size()));
        List<StatsInfoHolder> allHolders = new ArrayList<>(idsTostats.size());
        allHolders.addAll(idsTostats.values());
        List<Double> weights = new ArrayList(allHolders.size());
        double sum = 0;
        for (StatsInfoHolder statsInfoHolder: allHolders) {
            ChainNodeRoutingStats stats = statsInfoHolder.getStats();
            double currentWeight = 1; //start with weight 1
            if (stats != null){
                if (stats.getErrors() > 0){
                    currentWeight = currentWeight / (double) stats.getErrors();
                }
                if (stats.getMessagesProcessed() > 0){
                    currentWeight = currentWeight * (double) stats.getMessagesProcessed();
                }
                if (stats.getTimeSpentInSuccessfulRequests() > 0){
                    currentWeight = currentWeight / (double) stats.getTimeSpentInSuccessfulRequests();
                }
            }
            sum += currentWeight;
            weights.add(currentWeight);
        }
        //now, select [length] nodes based on weights:
        Random rnd = new Random(System.currentTimeMillis()+length);
        List<ChainNodeInfo> chainNodeInfos = new ArrayList<ChainNodeInfo>(length);
        for (int i = 0; i < length; i++){
            ChainNodeInfo newChainNode = null;
            while (newChainNode == null){
                ChainNodeInfo candidate = getChainNode(weights, allHolders, rnd.nextDouble() * sum);
                if (!chainNodeInfos.contains(candidate)){
                    newChainNode = candidate;
                }
            }
            chainNodeInfos.add(newChainNode);
        }
        return chainNodeInfos.toArray(new ChainNodeInfo[length]);
    }

    /**
     * Finds the chain node that corresponds to the specified randomValue.
     * @param weights
     * @param statsInfoHolders
     * @param randomValue
     * @return
     */
    private ChainNodeInfo getChainNode(List<Double> weights, List<StatsInfoHolder> statsInfoHolders, double randomValue) {
        double cumulWeight = 0.0;
        int i = 0;
        for (Double weight: weights){
            cumulWeight += weight;
            i++;
            if (cumulWeight > randomValue){
                return statsInfoHolders.get(i).getInfo();
            }
        }
        return statsInfoHolders.get(statsInfoHolders.size()).getInfo();
    }

    private class StatsInfoHolder {
        private ChainNodeRoutingStats stats;
        private ChainNodeInfo info;

        private StatsInfoHolder(ChainNodeRoutingStats stats, ChainNodeInfo info) {
            this.stats = stats;
            this.info = info;
        }

        public ChainNodeRoutingStats getStats() {
            return stats;
        }

        public void setStats(ChainNodeRoutingStats stats) {
            this.stats = stats;
        }

        public ChainNodeInfo getInfo() {
            return info;
        }

        public void setInfo(ChainNodeInfo info) {
            this.info = info;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StatsInfoHolder)) return false;

            StatsInfoHolder that = (StatsInfoHolder) o;

            if (info != null ? !info.equals(that.info) : that.info != null) return false;
            if (stats != null ? !stats.equals(that.stats) : that.stats != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = stats != null ? stats.hashCode() : 0;
            result = 31 * result + (info != null ? info.hashCode() : 0);
            return result;
        }
    }
}
