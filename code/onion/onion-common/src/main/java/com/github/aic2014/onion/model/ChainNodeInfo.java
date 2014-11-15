package com.github.aic2014.onion.model;

import java.net.URI;
import java.util.Date;

/**
 * Model class for communicating chain node information
 * between chain node and directory node via REST+JSON
 */
public class ChainNodeInfo {

    private String id = null;
    private String name = null;
    private String publicIP = null;
    private int port = 0;
    private Date launchedDate = null;
    private Date lastLifeCheck = null;
    //TODO: add information about the public key

    /**
     * Gets the ID of this chain node.
     * This property is supposed to be unique, compared to other ChainNodeInfo instances
     * E.g. i-a3f24b11
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
     * Gets the name of this chain node.
     * E.g. G6-T3-chainnode-4
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this chaine node-
     * @param name
     */
    public void setName(String name) {
        this.name = name;
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
}