package com.github.aic2014.onion.rest;

import com.github.aic2014.onion.model.ChainNodeInfo;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

/**
 * REST client that can be used to talk to the directory node.
 */
public class DirectoryNodeClient {

    private String directoryNodeURI;

    private RestTemplate restTemplate = new RestTemplate();

    public URI registerChainNode(ChainNodeInfo chainNodeInfo) {
        return restTemplate.postForLocation(directoryNodeURI + "/chainNode", chainNodeInfo);
    }

    public void unregisterChainNode(final URI chainNodeUri) {
        restTemplate.delete(chainNodeUri);
    }

    public void setDirectoryNodeURI(final String directoryNodeURI) {
        this.directoryNodeURI = directoryNodeURI;
    }

    public String getIPAdress(){
        // First try loading from the current directory
        String ip = "";
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
                ip = in.readLine();
                return ip;
            } finally {

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch ( Exception e ) {
            return ip;
        }
    }


}
