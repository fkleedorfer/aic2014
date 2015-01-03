package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.services.ec2.model.InstanceState;
import com.github.aic2014.onion.model.ChainNodeInfo;

/**
 * The AWSChainNode represents a PENDING or ACTIVE AWS instance which is (will be) used as a onion chain node
 */



public class AWSChainNode extends ChainNodeInfo{

    private String instanceName;
    private boolean scriptDone;


     /* InstanceState; public java.lang.Integer getCode()
    The low byte represents the state. The high byte is an opaque internal value and should be ignored.
    0 : pending
    16 : running
    32 : shutting-down
    48 : terminated
    64 : stopping
    80 : stopped
    */
    private InstanceState state;

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public boolean scriptDone() {
        return scriptDone;
    }

    public void setScriptDone(boolean scriptDone) {
        this.scriptDone = scriptDone;
    }

    public InstanceState getState(){
        return state;
    }

    public void setState(InstanceState state){
        this.state = state;
    }




    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AWSChainNode))
            return false;
        if (obj == this)
            return true;

        if (!this.getId().equals(null))
            return getId().equals(((AWSChainNode)obj).getId());
        else
            return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (getId() != null)
            return getId().hashCode();

        return super.hashCode();
    }
}