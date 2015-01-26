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

    private Object lock = new Object();

    private ConcurrentHashMap<String, StatsInfoHolder> idsTostats = new ConcurrentHashMap<>();

    public void deleteChainNode(String id){
        this.idsTostats.remove(id);
    }

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
        double weightSum = 0;
        //if we don't have data for load balancing, the algorithm will calculate a weight of 1, which is high.
        //therefore, we build a list of indices where the weight was calculated as 1 and set them to the average
        //afterwards.
        List<Integer> nodesWithWeight1 = new ArrayList<>(allHolders.size());
        int idx = 0;
        for (StatsInfoHolder statsInfoHolder: allHolders) {
            ChainNodeRoutingStats stats = statsInfoHolder.getStats();
            double currentWeight = 1.0; //start with weight 1
            if (stats != null){
                if (stats.getErrors() > 0 && stats.getTimeWindowSize() > 0){
                    currentWeight = currentWeight / (double) stats.getErrors() / (double) stats.getTimeWindowSize();
                }
                if (stats.getMessagesProcessed() > 0 && stats.getTimeWindowSize() > 0){
                    currentWeight = currentWeight * (double) stats.getMessagesProcessed() / (double) stats.getTimeWindowSize();
                }
                if (stats.getTimeSpentInSuccessfulRequests() > 0 && stats.getMessagesProcessed() > 0){
                    currentWeight = currentWeight /  ((double) stats.getTimeSpentInSuccessfulRequests() / (double) stats.getMessagesProcessed());
                }
            }
            if (currentWeight == 1.0){
                //we rembember this idx so we can set its weight to the average of the other weights
                nodesWithWeight1.add(idx);
            }
            statsInfoHolder.getInfo().setLastLoadBalancingWeight(currentWeight);
            weightSum += currentWeight;
            weights.add(currentWeight);
            idx++;
        }
        if (nodesWithWeight1.size() > 0 && nodesWithWeight1.size() < weights.size()){
            //adjust the weightSum
            weightSum -= nodesWithWeight1.size(); //subtract 1 for each node that got a weight of 1
            double averageWeight = weightSum / (weights.size() - nodesWithWeight1.size());
            for (Integer index: nodesWithWeight1){
                weights.set(index, averageWeight);
                //set it in the chain node info so we can show it in the gui
                allHolders.get(index).getInfo().setLastLoadBalancingWeight(averageWeight);
                //adjust again: add the average for each node
                weightSum += averageWeight;
            }
        }

        //now, select [length] nodes based on weights:
        Random rnd = new Random(System.currentTimeMillis()+length);
        List<ChainNodeInfo> chainNodeInfos = new ArrayList<ChainNodeInfo>(length);
        for (int i = 0; i < length; i++){
            //now select a chain node (actually, its index)
            int index = getChainNodeIndex(weights, allHolders, rnd.nextDouble() * weightSum);
            //add the chain node to the output and remove it from the allHolders and weights lists
            //(so we can't select it again)
            double weightLost = weights.remove(index);
            chainNodeInfos.add(allHolders.get(index).getInfo());
            allHolders.remove(index);
            weightSum -= weightLost;
        }
        for (ChainNodeInfo info: chainNodeInfos){
            updateSentMessages(info);
        }
        return chainNodeInfos.toArray(new ChainNodeInfo[length]);
    }

    private void updateSentMessages(ChainNodeInfo chainNodeInfo) {
        synchronized (lock){
            chainNodeInfo.setSentMessages(chainNodeInfo.getSentMessages()+1);
        }
    }

    /**
     * Finds the chain node that corresponds to the specified randomValue.
     * @param weights
     * @param statsInfoHolders
     * @param randomValue
     * @return
     */
    private int getChainNodeIndex(List<Double> weights, List<StatsInfoHolder> statsInfoHolders, double randomValue) {
        double cumulWeight = 0.0;
        int i = 0;
        for (Double weight: weights){
            cumulWeight += weight;
            if (cumulWeight > randomValue){
                return i;
            }
            i++;
        }
        return statsInfoHolders.size();
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
