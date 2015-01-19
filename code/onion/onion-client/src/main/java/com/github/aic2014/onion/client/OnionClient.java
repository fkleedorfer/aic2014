package com.github.aic2014.onion.client;

import com.github.aic2014.onion.crypto.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

/**
 * Client that creates Onion Routing Requests.
 */
public class OnionClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private URI directoryNodeUri;
    private int chainErrorRetries;

    private int connectTimeout;
    private int readTimeout;

    @Autowired
    private CryptoService cryptoService;

    public OnionRoutedHttpRequest getHttpRequest() {
        OnionRoutedHttpRequest req = new OnionRoutedHttpRequest(cryptoService, directoryNodeUri, chainErrorRetries);
        req.setTimeouts(connectTimeout, readTimeout);
        return req;
    }

    public void setDirectoryNodeUri(final URI directoryNodeUri) {
        this.directoryNodeUri = directoryNodeUri;
    }

    public void setChainErrorRetries(int chainErrorRetries) {
        this.chainErrorRetries = chainErrorRetries;
    }

    public void setTimeouts(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }
}
