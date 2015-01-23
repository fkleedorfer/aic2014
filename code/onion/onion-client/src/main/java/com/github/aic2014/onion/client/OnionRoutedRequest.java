package com.github.aic2014.onion.client;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.OnionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

public abstract class OnionRoutedRequest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RestTemplate restTemplate = new RestTemplate();
    protected ChainNodeInfo[] usedChain;
    protected UUID usedChainID;
    protected long roundTripTime = 0;

    protected CryptoService cryptoService;
    protected URI directoryNodeURI;
    protected int chainErrorRetries;

    public OnionRoutedRequest(CryptoService cryptoService, URI directoryNodeURI, int chainErrorRetries) {
        this.cryptoService = cryptoService;
        this.directoryNodeURI = directoryNodeURI;
        this.chainErrorRetries = chainErrorRetries;
    }

    /**
     * Sets the used timeouts for the REST connection to the first chain node.
     * @param connectTimeout timeout in milliseconds: abort if no connection established after timeout
     * @param readTimeout timeout in milliseconds: abort if no data received in connection timeout
     */
    public void setTimeouts(int connectTimeout, int readTimeout) {
        if (connectTimeout > 0)
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(connectTimeout);

        if (readTimeout > 0)
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
    }

    /**
     * Gets the chain nodes which were used for the succeeding request.
     */
    public ChainNodeInfo[] getUsedChain() {
        return usedChain;
    }

    /**
     * Gets the generated chain node ID.
     */
    public UUID getUsedChainID() {
        return usedChainID;
    }

    /**
     * Gets the round trip time of the request, in ms.
     */
    public long getRoundTripTime() {
        return roundTripTime;
    }

    protected void fetchNewChain() throws OnionRoutedRequestException {
        try {
            String requestUri = directoryNodeURI.toString() + "/getChain";
            logger.debug("requesting: {}", requestUri);
            ResponseEntity<ChainNodeInfo[]> newChainResult = restTemplate.getForEntity(requestUri, ChainNodeInfo[].class);
            usedChain = newChainResult.getBody();
            usedChainID = UUID.randomUUID();
            logger.debug("obtained new chain: {}", Arrays.toString(usedChain));

        } catch (Throwable e) {
            throw new OnionRoutedRequestException(OnionStatus.DIRECTORY_ERROR, e);
        }
    }

    public String printUsedChain() {
        String out = "-------------------------------\n";
        out += String.format("Used Chain (%s):\n", getUsedChainID());
        for (int i = 0; i < 3; i++) {
            out += String.format("ChainNode%d: %s:%d\n", i, getUsedChain()[i].getPublicIP(), getUsedChain()[i].getPort());
        }
        out += "-------------------------------";
        return out;
    }
}
