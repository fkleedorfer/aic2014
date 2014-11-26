package com.github.aic2014.onion.directorynode.aws;

/**
 * The AWSChainNode represents a PENDING or ACTIVE AWS instance which is (will be) used as a onion chain node
 */
public class AWSChainNode {

    private String instanceId;
    private String instanceName;
    private String publicIP;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}