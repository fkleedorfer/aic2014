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

    private final static String AWS_STATE_RUNNING = "running";
    private final static String AWS_STATE_PENDING = "pending";
    private final static String AWS_TAG_NAME = "Name";
    private final static int DEFAULT_NUM_CHAINS = 6;
    private final static int DEFAULT_MIN_CHAIN_SIZE = 3;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    private AmazonEC2 ec2;
    private int latestNodeNumber = 0;
    private int numberOfChainNodes;
    private int minNumberOfChainNodes;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String awsRegion;

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
        ec2 = new AmazonEC2Client(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey));
        ec2.setRegion(Region.getRegion(Regions.fromName(awsRegion)));

        //3. search for existing chain nodes
        List<AWSChainNode> existingChainNodes = readAWSChainNodes();
        logger.info("Found " + existingChainNodes.size() + " existing chain nodes on startup. Let's terminate them all!");

        //4. for now... after each start of the directory node, terminate all existing chain nodes.
        existingChainNodes.forEach(cni -> terminateChainNode(cni));
        AWSChainNode cn = null;

        //5. create new chain nodes
        createAWSChainNodes(numberOfChainNodes);
        awsChainNodes = readAWSChainNodes();
        logger.info("Created " + awsChainNodes.size() + " chain nodes within AWS.");

        //6. Run setup-script for new chainnodes
        ChainNodeInstaller cnInstaller = new ChainNodeInstaller();
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

        awsAccessKeyId = env.getProperty("aws.accesskeyid");
        awsSecretAccessKey = env.getProperty("aws.secretaccesskey");
        awsRegion = env.getProperty("aws.region");
    }

    /**
     * Gets all PENDING or ACTIVE chain nodes.
     * Results are not cached.
     * @return
     */
    private List<AWSChainNode> readAWSChainNodes() {

        ArrayList<AWSChainNode> awsChainNodes = new ArrayList<>();

        DescribeInstancesResult result = ec2.describeInstances();
        for (Reservation reservation : result.getReservations()) {
            for (Instance instance : reservation.getInstances()) {

                Optional<Tag> optional = instance.getTags().stream().filter((tag) ->
                                tag.getKey().equalsIgnoreCase(AWS_TAG_NAME) && tag.getValue().startsWith(env.getProperty("aws.chainnode.prefix"))
                ).findFirst();
                if (!optional.isPresent()) {
                    //current instance does not start with the chain-node-name-prefix... ignore
                    continue;
                }

                String id = instance.getInstanceId();
                String instanceName = optional.get().getValue();
                String publicIP = instance.getPublicIpAddress();
                InstanceState state = instance.getState();

                if (!(state.getName().equalsIgnoreCase(AWS_STATE_RUNNING) || state.getName().equalsIgnoreCase(AWS_STATE_PENDING))) {
                    //current instance is neither running nor starting... ignore
                    continue;
                }

                AWSChainNode awsCN = new AWSChainNode();
                awsCN.setInstanceId(id);
                awsCN.setInstanceName(instanceName);
                awsCN.setPublicIP(publicIP);
            }
        }

        return awsChainNodes;
    }

    /**
     * Terminates the given chain node
     * @param awsCN
     */
    private void terminateChainNode(AWSChainNode awsCN) {
        TerminateInstancesRequest request = new TerminateInstancesRequest();
        request.setInstanceIds(new ArrayList<String>() {{
            add(awsCN.getInstanceId());
        }});

        TerminateInstancesResult result = ec2.terminateInstances(request);
    }

    /**
     * Requests the creation of new chain node instances
     * @param quantity
     */
    private void createAWSChainNodes(int quantity) {

        RunInstancesRequest request = new RunInstancesRequest();
        request.withImageId(env.getProperty("aws.chainnode.defaultami"))
                .withInstanceType(InstanceType.fromValue(env.getProperty("aws.chainnode.type")))
                .withMinCount(quantity)
                .withMaxCount(quantity)
                .withKeyName(env.getProperty("aws.chainnode.keyname"))
                .withSecurityGroupIds(env.getProperty("aws.chainnode.securitygroup"))
                .withSubnetId(env.getProperty("aws.chainnode.subnet"));

        RunInstancesResult result = ec2.runInstances(request);
        for (Instance instance : result.getReservation().getInstances()) {
            CreateTagsRequest tagRequest = new CreateTagsRequest();
            tagRequest.withResources(instance.getInstanceId())
                    .withTags(new Tag(AWS_TAG_NAME, String.format("%s%d", env.getProperty("aws.chainnode.prefix"), ++latestNodeNumber)));
            ec2.createTags(tagRequest);
        }
    }
}