package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.github.aic2014.onion.model.ChainNodeInfo;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 *
 */
public class AWSConnector {

    public final static String AWS_STATE_RUNNING = "running";
    public final static String AWS_STATE_PENDING = "pending";
    public final static String AWS_TAG_NAME = "Name";

    private AmazonEC2 ec2;
    private Environment env;
    private List<AWSChainNode> awsChainNodes = Collections.synchronizedList(new ArrayList<AWSChainNode>());

    public AWSConnector(Environment env) {
        ec2 = new AmazonEC2Client(new BasicAWSCredentials(env.getProperty("aws.accesskeyid"),  env.getProperty("aws.secretaccesskey")));
        ec2.setRegion(Region.getRegion(Regions.fromName(env.getProperty("aws.region"))));
        this.env = env;
        this.awsChainNodes = new ArrayList<>();
    }


    public AWSChainNode getById(String instanceId) {
        Optional<AWSChainNode> result = this.awsChainNodes.stream().filter(cni -> cni.getId().equals(instanceId)).findAny();
        return result.isPresent() ? result.get() : null;
    }

    public void setPingTime(String instanceId, long pingTime) {
        synchronized (this.awsChainNodes){
             for (AWSChainNode awsChainNode : this.awsChainNodes){
                 if (awsChainNode.getId()== instanceId){
                     awsChainNode.setPingTime(pingTime);
                     break;
                 }
            }
        }
    }

    public void deleteAwsNode(String instanceName) {
        synchronized (this.awsChainNodes){
            for (AWSChainNode awsChainNode : this.awsChainNodes){
                if (awsChainNode.getInstanceName() == instanceName){
                    this.awsChainNodes.remove(awsChainNode);
                    break;
                }

            }
        }
    }


    private AWSChainNode findChainNodeByName(String name) {
        Optional<AWSChainNode> result = this.awsChainNodes.stream().filter(cni -> cni.getInstanceName().equals(name)).findAny();
        return result.isPresent() ? result.get() : null;
    }

    /**
     * Gets all PENDING or ACTIVE chain nodes.
     * Results are not cached.
     * @return
     */
    public List<AWSChainNode> getAllChainNodes(boolean allStarted) {

        synchronized (this) {

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
                    if(allStarted) {
                        if (!(state.getName().equalsIgnoreCase(AWS_STATE_RUNNING) || state.getName().equalsIgnoreCase(AWS_STATE_PENDING))) {
                            //current instance is neither running nor starting... ignore
                            continue;
                        }

                        AWSChainNode awsCN = this.getById(id);
                        if (awsCN == null) {
                            awsCN = new AWSChainNode();
                            awsCN.setId(id);
                            awsCN.setInstanceName(instanceName);
                            awsCN.setPublicIP(publicIP);
                            awsCN.setScriptDone(false);
                            awsCN.setState(state);
                            awsChainNodes.add(awsCN);
                        }

                    }else{
                        AWSChainNode awsCN = this.getById(id);
                        if (awsCN != null) {
                            awsCN.setState(state);
                            awsCN.setPublicIP(publicIP);
                        }
                    }
                }
            }
        }

        return new LinkedList<AWSChainNode>() {{
            awsChainNodes.forEach(cni -> add(cni));
        }};
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

        TerminateInstancesResult result = ec2.terminateInstances(request);

        //wenn die Node aufgrund von zu langem starten unregistriert wird muss sie natürlich nicht von der Liste entfernt werden
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
            this.deleteAwsNode(instanceName);

            AWSChainNode awsCN = this.getById(instance.getInstanceId());
            if (awsCN == null) {
                awsCN = new AWSChainNode();
                awsCN.setId(instance.getInstanceId());
                awsCN.setInstanceName(instanceName);
                awsCN.setScriptDone(false);
                awsCN.setState(instance.getState());
                awsChainNodes.add(awsCN);
            }
        }
    }
}