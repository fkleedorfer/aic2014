package com.github.aic2014.onion.client;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.crypto.RSAESCryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.URI;
import java.security.GeneralSecurityException;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/client.properties")
public class OnionClientConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${directorynode.baseUri}")
    private String directoryNodeBaseUri;

    @Value("${client.chainErrorRetries}")
    private int chainErrorRetries;

    @Value("${client.connectTimeout}")
    private int connectTimeout = -1;

    @Value("${client.readTimeout}")
    private int readTimeout = -1;

    @Bean
    public OnionClient getOnionClient() {
        OnionClient client = new OnionClient();
        client.setDirectoryNodeUri(URI.create(directoryNodeBaseUri));
        client.setChainErrorRetries(chainErrorRetries);
        client.setTimeouts(connectTimeout, readTimeout);
        return client;
    }

    @Bean
    public CryptoService getCryptoService() throws GeneralSecurityException {
        return new RSAESCryptoService();
    }

    @Bean
    public CommandLineRunner getOnionClientCommandLineRunner() {
        return new OnionClientCommandLineRunner();
    }


}
