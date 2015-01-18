package com.github.aic2014.onion.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URI;
import java.util.Date;

/**
 * Model class for communicating chain node information
 * between chain node and directory node via REST+JSON
 */
public class ChainNodeInfo {

    private String id = null;
    private String publicIP = null;
    private int port = 0;
    private Date lastLifeCheck = null;
    private String publicKey = null;
    private long pingTime = 0;
    private int sentMessages = 0;

    /**
     * Gets the ID of this chain node.
     * E.g. i-abbb8b63 (usually referred to as "Instance ID")
     * @return
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the public IP of this chain node
     * @return
     */
    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    /**
     * Gets the port number this chain-node is assigned to
     * @return
     */
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Date getLastLifeCheck() {
        return lastLifeCheck;
    }

    public void setLastLifeCheck(Date lastLifeCheck) {
        this.lastLifeCheck = lastLifeCheck;
    }

    /**
     * last voting, when initialised the Chain-Node has 100 votes: the maximum
     */
    public long getPingTime() {
        return pingTime;
    }

    public void setPingTime(long pingTime) {
        this.pingTime = pingTime;
    }

    public int getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(int sentMessages) {
        this.sentMessages = sentMessages;
    }

    @JsonIgnore
    public URI getUri() {
        if (publicIP == null || port == 0)
            return null;

        return URI.create("http://" + publicIP + ":" + port);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (!(o instanceof ChainNodeInfo))
            return false;

        final ChainNodeInfo that = (ChainNodeInfo) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * Gets the RSA encryption public key of the chainnode
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets the RSA encryption public key of the chainnode
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return String.format("ChainNodeInfo{id='%s', publicIP='%s', port=%d}", id, publicIP, port);
    }
}