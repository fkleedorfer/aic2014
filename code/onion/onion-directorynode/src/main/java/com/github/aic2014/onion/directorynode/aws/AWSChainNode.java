package com.github.aic2014.onion.directorynode.aws;

/**
 * The AWSChainNode represents a PENDING or ACTIVE AWS instance which is (will be) used as a onion chain node
 */
public class AWSChainNode {

    private String instanceId;
    private String instanceName;
    private String publicIP;
    private boolean isReady;

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

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AWSChainNode))
            return false;
        if (obj == this)
            return true;

        if (instanceId != null)
            return instanceId.equals(((AWSChainNode)obj).getInstanceId());
        else
            return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (instanceId != null)
            return instanceId.hashCode();

        return super.hashCode();
    }
}