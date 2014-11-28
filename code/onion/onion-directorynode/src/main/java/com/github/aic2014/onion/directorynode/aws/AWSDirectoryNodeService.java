package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.github.aic2014.onion.directorynode.DirectoryNodeService;
import com.github.aic2014.onion.model.ChainNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Concrete implementation of the directory node service using AWS SDK to access EC2 instances
 */
public class AWSDirectoryNodeService implements DirectoryNodeService {

    private final static int DEFAULT_NUM_CHAINS = 6;
    private final static int DEFAULT_MIN_CHAIN_SIZE = 3;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    private AWSConnector awsConnector;
    private int latestNodeNumber = 0;
    private int numberOfChainNodes;
    private int minNumberOfChainNodes;

    private List<ChainNodeInfo> chainNodes = new LinkedList<>();
    private List<AWSChainNode> awsChainNodes = new LinkedList<>();

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

        //3. search for existing chain nodes
        List<AWSChainNode> existingChainNodes = awsConnector.getAllChainNodes();
        logger.info("Found " + existingChainNodes.size() + " existing chain nodes on startup. Let's terminate them all!");

        //4. for now... after each start of the directory node, terminate all existing chain nodes.
        existingChainNodes.forEach(cni -> awsConnector.terminateChainNode(cni.getInstanceId()));
        AWSChainNode cn = null;

        //5. create new chain nodes
        String[] chainNodeNames = new String[numberOfChainNodes];
        for (int i = 0; i < numberOfChainNodes; i++, latestNodeNumber++) {
            chainNodeNames[i] = String.format("%s%d", env.getProperty("aws.chainnode.prefix"), i + latestNodeNumber);
        }
        awsConnector.createAWSChainNodes(numberOfChainNodes, chainNodeNames);
        awsChainNodes = awsConnector.getAllChainNodes();
        logger.info("Created " + awsChainNodes.size() + " chain nodes within AWS.");

        //6. Run setup-script for new chainnodes
        ChainNodeInstaller cnInstaller = new ChainNodeInstaller(env, awsConnector);
        cnInstaller.runInstallerFor(awsChainNodes);
    }

    @Override
    public String registerChainNode(ChainNodeInfo chainNodeInfo) {
        //TODO
        return null;
    }

    @Override
    public void unregisterChainNode(String id) {
        //TODO
    }

    @Override
    public ChainNodeInfo getChainNode(String id) {
        Optional<ChainNodeInfo> result = chainNodes.stream().filter(cni -> id.equals(cni.getId())).findAny();
        return result.isPresent() ? result.get() : null;
    }

    @Override
    public Collection<ChainNodeInfo> getAllChainNodes() {
        return new LinkedList<ChainNodeInfo>() {{
            chainNodes.forEach(cni -> add(cni));
        }};
    }

    @Override
    public List<ChainNodeInfo> getChain() {
        List<ChainNodeInfo> chain = new ArrayList<>(minNumberOfChainNodes);
        if (chainNodes.size() < minNumberOfChainNodes) {
            throw new IllegalStateException("At least " + minNumberOfChainNodes +
                    " chain nodes must be registered to build a chain. " +
                    "Currently registered: " + chainNodes.size());
        }

        List<ChainNodeInfo> randomChainNodes = new ArrayList<>(chainNodes.size());
        randomChainNodes.addAll(chainNodes);
        Collections.shuffle(randomChainNodes);

        randomChainNodes.stream().limit(minNumberOfChainNodes).forEach(cni -> chain.add(cni));
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
    }
}