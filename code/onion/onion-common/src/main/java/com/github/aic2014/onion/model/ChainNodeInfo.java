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
    private Date launchedDate = null;
    private Date lastLifeCheck = null;
    private String publicKey = null;

    /**
     * Gets the ID of this chain node.
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of this chain node.
     * @param id
     */
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

    /**
     * Sets the public IP of this chain node
     * @param publicIP
     */
    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Date getLaunchedDate() {
        return launchedDate;
    }

    public void setLaunchedDate(Date launchedDate) {
        this.launchedDate = launchedDate;
    }

    public Date getLastLifeCheck() {
        return lastLifeCheck;
    }

    public void setLastLifeCheck(Date lastLifeCheck) {
        this.lastLifeCheck = lastLifeCheck;
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