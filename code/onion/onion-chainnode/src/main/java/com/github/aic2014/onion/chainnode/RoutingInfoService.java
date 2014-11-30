package com.github.aic2014.onion.chainnode;

import com.github.aic2014.onion.model.RoutingInfo;

import java.util.*;

/**
 * Service managing RoutingInfo objects for debugging/demo purposes.
 *
 */
public class RoutingInfoService {

    private static final int CACHE_SIZE = 10;
    private LRUMap<UUID, RoutingInfo> routingInfos = new LRUMap<UUID, RoutingInfo>(CACHE_SIZE);

    private Comparator<RoutingInfo> routingInfoComparator = new Comparator<RoutingInfo>()  {
        @Override
        public int compare(RoutingInfo o1, RoutingInfo o2) {
            return o1.getLatestUpdate().compareTo(o2.getLatestUpdate());
        }
    };

    /**
     * Add a routingInfo object. If an object with the same chainId is present, it
     * is replaced.
     * @param routingInfo
     */
    public void updateRoutingInfo(RoutingInfo routingInfo){
        this.routingInfos.put(routingInfo.getChainId(), routingInfo);
    }

    /**
     * Get the list of currently stored RoutingInfo objects.
     * @return
     */
    public List<RoutingInfo> getRoutingInfo(){
        List<RoutingInfo> infos = new ArrayList<RoutingInfo>(routingInfos.size());
        infos.addAll(routingInfos.values());
        Collections.sort(infos, routingInfoComparator);
        return infos;
    }

    public RoutingInfo getRoutingInfo(UUID chainId){
        return routingInfos.get(chainId);
    }


    /**
     * Simple least-recently-used map that deletes the least recently used element
     * when it exceeds its capacity.
     * @param <K>
     * @param <V>
     */
    private class LRUMap<K,V> extends LinkedHashMap<K, V>{
        int capacity = Integer.MAX_VALUE;

        private LRUMap(int initialCapacity, float loadFactor, int capacity) {
            super(initialCapacity, loadFactor);
            this.capacity = capacity;
        }

        private LRUMap(int initialCapacity, int capacity) {
            super(initialCapacity);
            this.capacity = capacity;
        }

        private LRUMap(int capacity) {
            this.capacity = capacity;
        }

        private LRUMap(Map<? extends K, ? extends V> m, int capacity) {
            super(m);
            this.capacity = capacity;
        }

        private LRUMap(int initialCapacity, float loadFactor, boolean accessOrder, int capacity) {
            super(initialCapacity, loadFactor, accessOrder);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > CACHE_SIZE;
        }
    }
}
