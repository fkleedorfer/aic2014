package com.github.aic2014.onion.directorynode;

import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.ChainNodeRoutingStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@EnableScheduling
public class InMemoryDirectoryService implements DirectoryNodeService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int CHAIN_LENGTH = 3;
    private List<ChainNodeInfo> chainNodeInfos = Collections.synchronizedList(new ArrayList<ChainNodeInfo>());
    private RestTemplate restTemplate = new RestTemplate();
    private Object lock = new Object();
    private LoadBalancingChainCalculator loadBalancingChainCalculator = new LoadBalancingChainCalculator();

    public InMemoryDirectoryService() {
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(1000);
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(1000);
    }

    @Override
    public String registerChainNode(final ChainNodeInfo chainNodeInfo) {
        if (chainNodeInfo == null)
            return "null";

        chainNodeInfos.add(chainNodeInfo);
        chainNodeInfo.setId(UUID.randomUUID().toString());
        loadBalancingChainCalculator.registerChainNode(chainNodeInfo);
        return chainNodeInfo.getId();
    }

    @Override
    public void unregisterChainNode(String id) {
        assert id == null : "id must be non-null";
        this.chainNodeInfos.remove(findChainNodeInfo(id));
    }
    @Override
    public String getIPAddress(){
        return "127.0.0.1";
    }

    private ChainNodeInfo findChainNodeInfo(String id) {
        Optional<ChainNodeInfo> result = chainNodeInfos.stream().filter(cni -> cni.getId().equals(id)).findAny();
        return result.isPresent() ? result.get() : null;
    }

    @Override
    public ChainNodeInfo getChainNode(final String id) {
        assert id == null : "id must be non-null";
        return findChainNodeInfo(id);
    }

    @Override
    public Collection<ChainNodeInfo> getAllChainNodes() {
        List<ChainNodeInfo> copy = new ArrayList<ChainNodeInfo>(this.chainNodeInfos.size());
        copy.addAll(this.chainNodeInfos);
        return copy;
    }

    @Override
    public List<ChainNodeInfo> getChain() {
        ChainNodeInfo[] chainNodes = loadBalancingChainCalculator.getChain(CHAIN_LENGTH);
        for (int i = 0; i < chainNodes.length; i++){
            updateSentMessages(chainNodes[i]);
        }
        return Arrays.asList(chainNodes);
    }

  private void updateSentMessages(ChainNodeInfo chainNodeInfo) {
    synchronized (lock){
      chainNodeInfo.setSentMessages(chainNodeInfo.getSentMessages()+1);
    }
  }

    @Scheduled(fixedDelay=5000)
    public void LifeCheck() {

        //es müssen mindestens
        int countAlive = 0;
        Collection<ChainNodeInfo> myChainNodes = this.getAllChainNodes();

        for(ChainNodeInfo node: myChainNodes){
            try {
                ChainNodeRoutingStats stats = restTemplate.getForObject(node.getUri().toString() + "/ping", ChainNodeRoutingStats.class);
                this.loadBalancingChainCalculator.updateStats(stats, node);
            } catch (Exception e){
                logger.debug("caught exception while trying to ping node "+node.getId(),e);
            }

        }
    }
}
