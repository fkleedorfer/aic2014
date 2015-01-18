package com.github.aic2014.onion.directorynode.aws;

import com.github.aic2014.onion.directorynode.DirectoryNodeService;
import com.github.aic2014.onion.model.ChainNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Concrete implementation of the directory node service using AWS SDK to access EC2 instances
 */
@EnableScheduling
public class AWSDirectoryNodeService implements DirectoryNodeService {


    class AWSChainNodeChainedComparator implements Comparator<AWSChainNode> {

        private List<Comparator<AWSChainNode>> listComparators;

        @SafeVarargs
        public AWSChainNodeChainedComparator(Comparator<AWSChainNode>... comparators) {
            this.listComparators = Arrays.asList(comparators);
        }

        @Override
        public int compare(AWSChainNode awsChainNode1, AWSChainNode awsChainNode2) {
            for (Comparator<AWSChainNode> comparator : listComparators) {
                int result = comparator.compare(awsChainNode1, awsChainNode2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    }
    class AWSChainNodeSentMessagesComparator implements Comparator<AWSChainNode> {

        @Override
        public int compare(AWSChainNode awsChainNode1, AWSChainNode awsChainNode2) {
            return awsChainNode1.getSentMessages() - awsChainNode2.getSentMessages();
        }
    }
    class AWSChainNodePingTimeComparator implements Comparator<AWSChainNode> {

        @Override
        public int compare(AWSChainNode awsChainNode1, AWSChainNode awsChainNode2) {
            return (int)(awsChainNode1.getPingTime() - awsChainNode2.getPingTime());
        }
    }



    private final static int DEFAULT_NUM_CHAINS = 6;
    private final static int DEFAULT_MIN_CHAIN_SIZE = 3;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    private AWSConnector awsConnector;
    private boolean terminateExisting = false;
    private int latestNodeNumber = 0;
    private int numberOfChainNodes;
    private int minNumberOfChainNodes;

    /**
     * Initializes the AWS Directory Node Service
     */
    @PostConstruct
    public void onInit() {

        //
        //1. read configuration
        initConfiguration();

        //
        //2. init AWS-EC2 client
        awsConnector = new AWSConnector(env);
        synchronized (awsConnector) {

            //3. search for existing chain nodes
            if (terminateExisting) {
                List<AWSChainNode> existingChainNodes = awsConnector.getAllChainNodes(true);
                logger.info("Found " + existingChainNodes.size() + " existing chain nodes on startup. Let's terminate them all!");

                //4. for now... after each start of the directory node, terminate all existing chain nodes.
                existingChainNodes.forEach(cni -> awsConnector.terminateChainNode(cni.getId(), true));


                //5. create new chain nodes
                String[] chainNodeNames = new String[numberOfChainNodes];
                for (int i = 0; i < numberOfChainNodes; i++) {
                    int serial = latestNodeNumber + i;
                    chainNodeNames[i] = String.format("%s%d", env.getProperty("aws.chainnode.prefix"), serial);
                }
                latestNodeNumber += numberOfChainNodes;
                awsConnector.createAWSChainNodes(numberOfChainNodes, chainNodeNames);
            }
        }
        logger.info("Created/found " + latestNodeNumber+1 + " chain nodes within AWS.");

        //6. Run setup-script for new chainnodes
        ChainNodeInstaller cnInstaller = new ChainNodeInstaller(env, awsConnector);
        //der Thread  läuft Liste wird vom Connector geladen
        //cnInstaller.runInstallerFor(awsChainNodes);
    }

    //TODO neue Chainode über Connector starten und in Liste speichern
    @Override
    public String registerChainNode(ChainNodeInfo chainNodeInfo) {

        Collection<AWSChainNode> awsChainNodes = (this).getAllAWSChainNodes();

        if (chainNodeInfo == null) {
            logger.warn("NULL chain node tried to register. Ignore!");
            return null;
        }

        AWSChainNode equivalentAWSCN = null;
        for (AWSChainNode awsCN : awsChainNodes) {
            if (awsCN.getPublicIP() == null || chainNodeInfo.getPublicIP() == null) {
                continue;
            }
            if (awsCN.getPublicIP().equals(chainNodeInfo.getPublicIP())) {
                equivalentAWSCN = awsCN;
                break;
            }
        }

        if (equivalentAWSCN == null) {
            logger.warn(String.format("Unknown chain node with IP %s tried to register. Ignore!", chainNodeInfo.getPublicIP()));
            return null;
        }

        logger.info(String.format("Register chain node with IP %s.", chainNodeInfo.getPublicIP()));
        chainNodeInfo.setId(equivalentAWSCN.getId());

        //connector createinstances
        //this.awsChainNodes.add((AWSChainNode)chainNodeInfo);
        return chainNodeInfo.getId();
    }

    //TODO in connector
    @Override
    public void unregisterChainNode(String id) {
        /*
        synchronized(awsChainNodes) {
            awsChainNodes.removeIf(cn -> cn.getId().equals(id));
        }
        */
    }

    @Override
    public ChainNodeInfo getChainNode(String id) {
        Optional<AWSChainNode> result = awsConnector.getAllChainNodes(false).stream().filter(cni -> id.equals(cni.getId())).findAny();
        return result.isPresent() ? result.get() : null;
    }

    @Override
    public Collection<ChainNodeInfo> getAllChainNodes() {
        return new LinkedList<ChainNodeInfo>() {{
            awsConnector.getAllChainNodes(false).forEach(cni -> add(cni));
        }};
    }

    public Collection<AWSChainNode> getAllAWSChainNodes() {
        return new LinkedList<AWSChainNode>() {{
            awsConnector.getAllChainNodes(false).forEach(cni -> add(cni));
        }};
    }

    @Override
    public List<ChainNodeInfo> getChain() {

        List<AWSChainNode> awsChainNodes;

        synchronized (this.awsConnector) {
            awsChainNodes = this.awsConnector.getAllChainNodes(false);
        }

        List<ChainNodeInfo> chain = new ArrayList<>(minNumberOfChainNodes);
        if (awsChainNodes.size() < minNumberOfChainNodes) {
            throw new IllegalStateException("At least " + minNumberOfChainNodes +
                    " chain nodes must be registered to build a chain. " +
                    "Currently registered: " + awsChainNodes.size());
        }

        Collections.sort(awsChainNodes, new AWSChainNodeChainedComparator(
                        new AWSChainNodeSentMessagesComparator(),
                        new AWSChainNodePingTimeComparator()
        ));

        for (int i = 0;i < minNumberOfChainNodes; i++) {
            awsChainNodes.get(i).setSentMessages(awsChainNodes.get(i).getSentMessages()+1);
            chain.add(awsChainNodes.get(i));
        }

        return chain;
    }

    /**
     * Performs all configuration-related tasks.
     */
    private void initConfiguration() {
        try {
            numberOfChainNodes = Integer.parseInt(env.getProperty("aws.chainnode.quantity"));
        } catch (NumberFormatException e) { numberOfChainNodes = DEFAULT_NUM_CHAINS; }
        try {
            minNumberOfChainNodes = Integer.parseInt(env.getProperty("aws.chainnode.minQuantity"));
        } catch (NumberFormatException e) { minNumberOfChainNodes = DEFAULT_MIN_CHAIN_SIZE; }

        terminateExisting = Boolean.parseBoolean(env.getProperty("aws.terminateExisting"));
    }


    @Scheduled(fixedDelay=5000)
    public void LifeCheck() {

        //es müssen mindestens
        int countAlive = 0;
        Collection<AWSChainNode> aWSChainNodes = (this).getAllAWSChainNodes();

        InetAddress inetAddress = null;

        Date start, stop;
        Iterator itAwsChainNode = aWSChainNodes.iterator();
        AWSChainNode aWSChainNode;
        while (  itAwsChainNode.hasNext()) {
            aWSChainNode = (AWSChainNode)itAwsChainNode.next();

            //instance is running check responsetime  16 : running
            if ((aWSChainNode.getState()!= null)&& (aWSChainNode.getState().getCode() == 16)) {
                try {
                    inetAddress = InetAddress.getByName(aWSChainNode.getPublicIP());
                } catch (UnknownHostException e) {
                    logger.error("LifeCheck Error: Unknown host ChainNode {} will be removed.", aWSChainNode.getInstanceName());
                    //this.directoryNodeService.unregisterChainNode(chainNodeInfo.getId());
                    continue;
                }

                try {
                    start = new Date();
                    if (inetAddress.isReachable(5000)) {
                        stop = new Date();

                        synchronized (this.awsConnector) {
                            this.awsConnector.setPingTime(aWSChainNode.getId(),stop.getTime() - start.getTime() );
                        }
                    }
                } catch (IOException | IllegalArgumentException e1) {
                    logger.error("LifeCheck Error: An error has occurred, ChainNode {} will be removed.", aWSChainNode.getId());
                    //this.directoryNodeService.unregisterChainNode(chainNodeInfo.getId());
                    continue;
                }
            }
        }
    }
     /* InstanceState; public java.lang.Integer getCode()
    The low byte represents the state. The high byte is an opaque internal value and should be ignored.
    0 : pending
    16 : running

    */

}