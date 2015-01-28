package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.services.ec2.model.InstanceState;
import com.github.aic2014.onion.model.ChainNodeInfo;

/**
 * The AWSChainNode represents a PENDING or ACTIVE AWS instance which is (will be) used as a onion chain node
 */
public class AWSChainNode extends ChainNodeInfo{

    private String instanceName;
    private boolean isInstalling = false;
    private boolean isRegistered = false;
    private boolean isScheduledForShuttingDown = false;

    private AWSState awsState = null;

    /**
     * Gets the AWS-name of this instance.
     * E.g. "G6-T3-chainnode-4"
     * @return
     */
    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    /**
     * Gets whether the chain nodes has successfully registered itself at
     * the directory node or not
     * @return
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * True if the chain node is been installed right now
     * @return
     */
    public boolean isInstalling() {
        return isInstalling;
    }

    /**
     * True if thie chain nodes has been scheduled to shut down
     * (e.g. from the life checker)
     *
     * @return
     */
    public boolean isScheduledForShuttingDown() {
        return isScheduledForShuttingDown;
    }

    public void confirmRegistration() {
        this.isRegistered = true;
        this.isInstalling = false;
    }

    public void confirmInstalling() {
        this.isRegistered = false;
        this.isInstalling = true;
    }

    public void scheduleShutdown() {
        this.isScheduledForShuttingDown = true;
    }

    public AWSState getAWSState() {
        return awsState;
    }

    public void updateAWSState(InstanceState state) {
        awsState = AWSState.fromState(state);
    }
}