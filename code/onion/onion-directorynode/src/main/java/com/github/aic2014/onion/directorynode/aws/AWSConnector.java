package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.github.aic2014.onion.directorynode.LoadBalancingChainCalculator;
import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.ChainNodeRoutingStats;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * Providing relevant and simplifed access to the AWS EC2 API
 * Maintaining an internal list of AWSChainNodes.
 */
public class AWSConnector {

    public final static String AWS_TAG_NAME = "Name";

    private AmazonEC2 ec2;
    private Environment env;
    private List<AWSChainNode> awsChainNodes;
    private LoadBalancingChainCalculator loadBalancingChainCalculator;

    /**
     * Initializes the AWSConnector Object.
     *
     * @param env
     */
    public AWSConnector(Environment env) {
        ec2 = new AmazonEC2Client(new BasicAWSCredentials(env.getProperty("aws.accesskeyid"),  env.getProperty("aws.secretaccesskey")));
        ec2.setRegion(Region.getRegion(Regions.fromName(env.getProperty("aws.region"))));
        this.env = env;
        awsChainNodes = Collections.synchronizedList(new ArrayList<AWSChainNode>());
        loadBalancingChainCalculator = new LoadBalancingChainCalculator();
    }

    /**
     * Finds the corresponding Chain Node from the given public IP.
     * Lookup ONLY within the internal list
     *
     * @param ip
     * @return
     */
    public AWSChainNode findChainNodeByIP(String ip) {
        Optional<AWSChainNode> result = this.awsChainNodes.stream().filter(cni -> cni.getPublicIP().equals(ip)).findAny();
        return result.isPresent() ? result.get() : null;
    }

    public void registerRoutingStatus(AWSChainNode awsCN){
        this.loadBalancingChainCalculator.registerChainNode(awsCN);
    }

    public void updateRoutingStatus(AWSChainNode awsCN, ChainNodeRoutingStats stats) {
        this.loadBalancingChainCalculator.updateStats(stats, awsCN);
    }

    public void loadBalancerDeleteNode(String id){
        this.loadBalancingChainCalculator.deleteChainNode(id);
    }

    public ChainNodeInfo[] getChain(int length) {
        ChainNodeInfo [] chainNodeInfos = this.loadBalancingChainCalculator.getChain(length);
        ChainNodeInfo chainNodeInfo;
        for (int i = 0; i < length; i++){
            chainNodeInfo = this.findChainNodeByIP(chainNodeInfos[i].getPublicIP());
            chainNodeInfo.setSentMessages(chainNodeInfo.getSentMessages() + 1);
        }
        return this.loadBalancingChainCalculator.getChain(length);
    }

    public List<AWSChainNode> getInternalChainNodeList() {
        return new LinkedList<AWSChainNode>() {{
            awsChainNodes.stream().forEach(cni -> add(cni));
        }};
    }

    /**
     * Gets (ALL) || (PENDING or ACTIVE) chain nodes.
     * It also updates the internal list of chain nodes
     *
     * @return new List with references to the existing chain nodes
     */
    public List<AWSChainNode> getAllChainNodes(boolean onlyRunning) {

        LinkedList<AWSChainNode> allChainNodes = new LinkedList<>();
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

                //hier werden die existierenden laufenden Instanzen gesucht nur in diesem Fall werden die Nodes hier der Liste zugefügt ansonsten passiert das nur wenn
                //eine neue Instanz erstellt wird.
                if (onlyRunning && !(AWSState.fromState(state) == AWSState.RUNNING || AWSState.fromState(state) == AWSState.PENDING)) {
                    //current instance is neither RUNNING nor PENDING... ignore
                    continue;
                }

                AWSChainNode awsCN = new AWSChainNode();
                awsCN.setId(id);
                awsCN.setInstanceName(instanceName);
                awsCN.setPublicIP(publicIP);
                awsCN.updateAWSState(state);
                awsCN.setLastLifeCheck(new Date());

                allChainNodes.add(awsCN);
            }
        }

        return allChainNodes;
    }

    public void updateCurrentChainNodes() {
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
                String publicIP = instance.getPublicIpAddress();
                InstanceState state = instance.getState();

                AWSChainNode awsCN = getById(id);
                if (awsCN == null)
                    continue;

                awsCN.updateAWSState(state);
                awsCN.setPublicIP(publicIP);
                awsCN.setLastLifeCheck(new Date());
            }
        }
    }

    /**
     * Terminates the given chain node
     *
     * @param instanceId AWS instance id
     */
    public void terminateChainNode(String instanceId, boolean removeFromList) {
        TerminateInstancesRequest request = new TerminateInstancesRequest();
        request.setInstanceIds(new ArrayList<String>() {{
            add(instanceId);
        }});

        ec2.terminateInstances(request);

        //when the instance is reloaded the element will be deleted, so information can still be received in time between
        if (removeFromList){
            AWSChainNode awsCN = this.getById(instanceId);
            this.awsChainNodes.remove(awsCN);
        }
    }

    /**
     * Requests the creation of new chain node instances
     *
     * @param quantity
     * @param names
     */
    public void createAWSChainNodes(int quantity, String[] names) {

        RunInstancesRequest request = new RunInstancesRequest();
        request.withImageId(env.getProperty("aws.chainnode.defaultami"))
                .withInstanceType(InstanceType.fromValue(env.getProperty("aws.chainnode.type")))
                .withMinCount(quantity)
                .withMaxCount(quantity)
                .withKeyName(env.getProperty("aws.chainnode.keyname"))
                .withSecurityGroupIds(env.getProperty("aws.chainnode.securitygroup"))
                .withSubnetId(env.getProperty("aws.chainnode.subnet"));

        RunInstancesResult result = ec2.runInstances(request);

        int counter = 0;
        for (Instance instance : result.getReservation().getInstances()) {
            String instanceName = counter >= names.length ? "#undef#" : names[counter];
            counter++;

            CreateTagsRequest tagRequest = new CreateTagsRequest();
            tagRequest.withResources(instance.getInstanceId()).withTags(new Tag(AWS_TAG_NAME, instanceName));
            ec2.createTags(tagRequest);

            //wenn die Node mit dem Instanznamen schon rennt wirds von der Liste gelöscht, heruntergefahren is se ja schon
            deleteAwsNodeFromList(instanceName);

            AWSChainNode awsCN = new AWSChainNode();
            awsCN.setId(instance.getInstanceId());
            awsCN.setInstanceName(instanceName);
            awsCN.setPort(Integer.parseInt(env.getProperty("aws.chainnode.port")));
            awsCN.updateAWSState(instance.getState());
            awsCN.setLaunchedDate(new Date());
            awsChainNodes.add(awsCN);
        }
    }

    /**
     * Finds the corresponding AWSChainNode from the chain node list
     * @param instanceId
     * @return
     */
    private AWSChainNode getById(String instanceId) {
        Optional<AWSChainNode> result = this.awsChainNodes.stream().filter(cni -> cni.getId().equals(instanceId)).findAny();
        return result.isPresent() ? result.get() : null;
    }

    /**
     * Removes the given chain node from the internal list
     * @param instanceName
     */
    private void deleteAwsNodeFromList(String instanceName) {
        for (AWSChainNode awsChainNode : this.awsChainNodes){
            if (awsChainNode.getInstanceName().equals(instanceName)) {
                this.awsChainNodes.remove(awsChainNode);
                return;
            }
        }
    }
}