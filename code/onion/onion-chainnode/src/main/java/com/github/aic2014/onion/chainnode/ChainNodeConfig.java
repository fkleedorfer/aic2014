package com.github.aic2014.onion.chainnode;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.crypto.RSAESCryptoService;
import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.rest.DirectoryNodeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;

@Configuration
@EnableAutoConfiguration
@EnableAsync
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/chainnode.properties")
public class ChainNodeConfig extends AsyncConfigurerSupport {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${directorynode.baseUri}")
    private String directoryNodeBaseUri;

    @Value("${detectLocalIp.viaAmazonAWS}")
    private boolean detectLocalIpViaAmazonAWS = true;

    private URI chainNodeUri;

    @Override
    public Executor getAsyncExecutor() {
        //important: the number of threads should be greater than
        //the number of threads allowed by the application server
        //controlled by ${server.tomcat.max-threads}
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(150);
        executor.initialize();
        return executor;
    }

    /**
     * Bean that registers an application listener. When the application container starts up
     * the listener is called and retrieves the host/port from the container and
     * registers with the directory node.
     *
     * @return
     */
    @Bean
    public ApplicationListener<EmbeddedServletContainerInitializedEvent> getPortDiscoveryBean(final DirectoryNodeClient client) {
        ApplicationListener<EmbeddedServletContainerInitializedEvent> listener = new
                ApplicationListener<EmbeddedServletContainerInitializedEvent>() {
                    @Override
                    public void onApplicationEvent(final EmbeddedServletContainerInitializedEvent embeddedServletContainerInitializedEvent) {
                        EmbeddedServletContainer container = embeddedServletContainerInitializedEvent.getEmbeddedServletContainer();
                        int port = container.getPort();
                        logger.info("servlet container initialized, port is {}", port);
                        try {
                            //wir benütigen die public ip um zu wissen um welchen chainnode es geht und den public key
                            ChainNodeInfo chainNodeInfo = new ChainNodeInfo();
                            if (detectLocalIpViaAmazonAWS){
                                chainNodeInfo.setPublicIP(client.getIPAdress());
                            } else  {
                                chainNodeInfo.setPublicIP(InetAddress.getLocalHost().getHostAddress());
                            }
                            chainNodeInfo.setPort(port);
                            chainNodeInfo.setPublicKey(getCryptoService().getPublicKey());
                            chainNodeUri = client.registerChainNode(chainNodeInfo);
                            logger.info("chain node registered, obtained this URI: {}" + " Port " + port, chainNodeUri);
                        } catch (GeneralSecurityException e) {
                            logger.warn("crypto service security error", e);
                        } catch (UnknownHostException e) {
                            logger.warn("could not obtain local ip address", e);
                        }

                    }
                };
        return listener;
    }


    @Bean
    public ApplicationListener<ContextClosedEvent> getUnregisterOnContextEventBean(final DirectoryNodeClient client) {
        ApplicationListener<ContextClosedEvent> listener = new
                ApplicationListener<ContextClosedEvent>() {
                    @Override
                    public void onApplicationEvent(final ContextClosedEvent contextClosedEvent) {
                        client.unregisterChainNode(chainNodeUri);
                        logger.debug("chain node {} unregistered", chainNodeUri);
                    }
                };
        return listener;
    }

    @Bean
    public DirectoryNodeClient getDirectoryNodeClient() {
        DirectoryNodeClient client = new DirectoryNodeClient();
        client.setDirectoryNodeURI(directoryNodeBaseUri);
        return client;
    }

    @Bean
    public CryptoService getCryptoService() throws GeneralSecurityException {
        return new RSAESCryptoService();
    }

    @Bean
    AsyncRequestService getExitRequestService() {
        return new AsyncRequestService();
    }

    @Bean
    RoutingInfoService getRoutingInfoService() {
        return new RoutingInfoService();
    }


}
