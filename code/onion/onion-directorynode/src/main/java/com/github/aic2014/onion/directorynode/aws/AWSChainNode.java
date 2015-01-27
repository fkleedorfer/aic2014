package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.services.ec2.model.InstanceState;
import com.github.aic2014.onion.model.ChainNodeInfo;

/**
 * The AWSChainNode represents a PENDING or ACTIVE AWS instance which is (will be) used as a onion chain node
 */
public class AWSChainNode extends ChainNodeInfo{

    private String instanceName;
    private boolean Started;
    private boolean ShuttingDown;

    /**
     * InstanceState; public java.lang.Integer getCode()
     * The low byte represents the state. The high byte is an opaque internal value and should be ignored.
     * 0 : pending
     * 16 : running
     * 32 : shutting-down
     * 48 : terminated
     * 64 : stopping
     * 80 : stopped
     */
    private InstanceState state;

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

    public boolean isStarted() {
        return Started;
    }

    public void setStarted(boolean Started) {
        this.Started = Started;
    }

    public boolean isShuttingDown() {
        return ShuttingDown;
    }

    public void setShuttingDown(boolean ShuttingDown) {
        this.ShuttingDown = ShuttingDown;
    }

    public InstanceState getState(){
        return state;
    }

    public void setState(InstanceState state){
        this.state = state;
    }
}