package com.github.aic2014.onion.directorynode.aws;

import com.amazonaws.services.ec2.model.InstanceState;

/**
 * Mapping the AWS-States into a more readable enum
 *
 * From AWS JavaDoc:
 *      InstanceState; public java.lang.Integer getCode()
 *      The low byte represents the state. The high byte is an opaque internal value and should be ignored.
 *      0 : pending
 *      16 : running
 *      32 : shutting-down
 *      48 : terminated
 *      64 : stopping
 *      80 : stopped
 */
public enum AWSState {
    PENDING(0),
    RUNNING(16),
    SHUTTING_DOWN(32),
    TERMINATED(48),
    STOPPING(64),
    STOPPED(80);

    private int code;

    private AWSState(int code) {
        this.code = code;
    }

    public static AWSState fromState(InstanceState instanceState) {
        switch (instanceState.getCode()) {
            case  0: return PENDING;
            case 16: return RUNNING;
            case 32: return SHUTTING_DOWN;
            case 48: return TERMINATED;
            case 64: return STOPPING;
            case 80: return STOPPED;
            default: return null;
        }
    }

    public boolean equals(InstanceState instanceState) {
        return (code == instanceState.getCode());
    }
}
