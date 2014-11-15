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
import org.springframework.web.context.request.RequestScope;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Stream;

/**
 * Concrete implementation of the directory node service using AWS SDK to access EC2 instances
 * Created by fabthe on 15.11.2014.
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

    //private EC2 ec2;
    private AmazonEC2 ec2;
    private List<ChainNodeInfo> chainNodes = new LinkedList<>();
    private int latestNodeNumber = 0;
    private int numberOfChainNodes;
    private int minNumberOfChainNodes;

    /**
     * Initializes the AWS Directory Node Service
     */
    @PostConstruct
    public void onInit() {

        try {
            numberOfChainNodes = Integer.parseInt(env.getProperty("aws.chainnode.quantity"));
        } catch (NumberFormatException e) { numberOfChainNodes = DEFAULT_NUM_CHAINS; }
        try {
            minNumberOfChainNodes = Integer.parseInt(env.getProperty("aws.chainnode.minQuantity"));
        } catch (NumberFormatException e) { minNumberOfChainNodes = DEFAULT_MIN_CHAIN_SIZE; }

        ec2 = new AmazonEC2Client(new BasicAWSCredentials(env.getProperty("aws.accesskeyid"), env.getProperty("aws.secretaccesskey")));
        ec2.setRegion(Region.getRegion(Regions.fromName(env.getProperty("aws.region"))));

        updateChainNodeList();
        logger.info("Found " + chainNodes.size() + " existing chain nodes on startup. Let's terminate them all!");

        // for now... after each start of the directory node, let us terminate all existing chain nodes.
        chainNodes.forEach(cni -> shutdownNode(cni));

        // create
        createChainNode(numberOfChainNodes);
        updateChainNodeList();
        logger.info("Created " + chainNodes.size() + " chain nodes.");
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
     * Adds or updates the internal chain-node-list.
     * For anyone confused by the Reservations: http://stackoverflow.com/questions/15618825/what-is-the-purpose-of-reservations-in-amazon-ec2
     * TODO: delete missing chain nodes.
     */
    private void updateChainNodeList() {

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
                Date launchedDate = instance.getLaunchTime();

                if (!(state.getName().equalsIgnoreCase(AWS_STATE_RUNNING) || state.getName().equalsIgnoreCase(AWS_STATE_PENDING))) {
                    //current instance is neither running nor starting... ignore
                    continue;
                }

                ChainNodeInfo cni = null;
                Optional<ChainNodeInfo> existingCNI = chainNodes.stream().filter(existingcni -> existingcni.getId().equals(instanceName)).findFirst();
                if (existingCNI.isPresent())
                    cni = existingCNI.get();
                else {
                    cni = new ChainNodeInfo();
                    chainNodes.add(cni);
                }

                cni.setId(id);
                cni.setName(instanceName);
                cni.setPublicIP(publicIP);
                cni.setLaunchedDate(launchedDate);
            }
        }
    }

    private void shutdownNode(ChainNodeInfo cni) {
        TerminateInstancesRequest request = new TerminateInstancesRequest();
        request.setInstanceIds(new ArrayList<String>() {{
            add(cni.getId());
        }});

        TerminateInstancesResult result = ec2.terminateInstances(request);
        chainNodes.remove(cni);
    }

    private void createChainNode(int quantity) {

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