package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class AWSConnector {

    public final static String AWS_STATE_RUNNING = "running";
    public final static String AWS_STATE_PENDING = "pending";
    public final static String AWS_TAG_NAME = "Name";

    private AmazonEC2 ec2;
    private Environment env;

    public AWSConnector(Environment env) {
        ec2 = new AmazonEC2Client(new BasicAWSCredentials(env.getProperty("aws.accesskeyid"),  env.getProperty("aws.secretaccesskey")));
        ec2.setRegion(Region.getRegion(Regions.fromName(env.getProperty("aws.region"))));
        this.env = env;
    }

    public AWSChainNode getById(String instanceId) {
        List<AWSChainNode> awsChainNodes = getById(new ArrayList<String>(1){{ add(instanceId); }});
        if (awsChainNodes.isEmpty())
            return null;

        return awsChainNodes.get(0);
    }

    public List<AWSChainNode> getById(List<String> instanceIds) {
        ArrayList<AWSChainNode> awsChainNodes = new ArrayList<>();

        DescribeInstancesResult result = ec2.describeInstances();
        for (Reservation reservation : result.getReservations()) {
            for (Instance instance : reservation.getInstances()) {

                Optional<Tag> optional = instance.getTags().stream().filter((tag) -> tag.getKey().equalsIgnoreCase(AWS_TAG_NAME)).findFirst();

                if (!instanceIds.contains(instance.getInstanceId())) {
                    continue;
                }

                String id = instance.getInstanceId();
                String instanceName = optional.isPresent() ? optional.get().getValue() : null;
                String publicIP = instance.getPublicIpAddress();
                InstanceState state = instance.getState();

                AWSChainNode awsCN = new AWSChainNode();
                awsCN.setInstanceId(id);
                awsCN.setInstanceName(instanceName);
                awsCN.setPublicIP(publicIP);
                awsCN.setReady(state.getName().equalsIgnoreCase(AWS_STATE_RUNNING));
                awsChainNodes.add(awsCN);
            }
        }

        return awsChainNodes;
    }

    /**
     * Gets all PENDING or ACTIVE chain nodes.
     * Results are not cached.
     * @return
     */
    public List<AWSChainNode> getAllChainNodes() {
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
                awsCN.setReady(state.getName().equalsIgnoreCase(AWS_STATE_RUNNING));
                awsChainNodes.add(awsCN);
            }
        }

        return awsChainNodes;
    }

    /**
     * Terminates the given chain node
     *
     * @param instanceId AWS instance id
     */
    public void terminateChainNode(String instanceId) {
        TerminateInstancesRequest request = new TerminateInstancesRequest();
        request.setInstanceIds(new ArrayList<String>() {{
            add(instanceId);
        }});

        TerminateInstancesResult result = ec2.terminateInstances(request);
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
            CreateTagsRequest tagRequest = new CreateTagsRequest();
            tagRequest.withResources(instance.getInstanceId())
                    .withTags(new Tag(AWS_TAG_NAME, names[counter]));
            ec2.createTags(tagRequest);
        }
    }
}