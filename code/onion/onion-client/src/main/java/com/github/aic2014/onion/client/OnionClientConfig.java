package com.github.aic2014.onion.client;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.crypto.DummyCryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/client.properties")
public class OnionClientConfig
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${directorynode.baseUri}")
  private String directoryNodeBaseUri;


  @Bean
  public OnionClient getOnionClient(){
    OnionClient client = new OnionClient();
    client.setDirectoryNodeUri(URI.create(directoryNodeBaseUri));
    //TODO: remove the quote server URI - it will be specified by the caller
    client.setQuoteServerUri(URI.create("localhost:20141"));
    return client;
  }

  @Bean
  public CryptoService getCryptoService() throws GeneralSecurityException {
    return new DummyCryptoService();
  }

  @Bean
  public PendingResponseService getPendingResponseService(){
    return new PendingResponseService();
  }


  /**
   * Bean that registers an application listener. When the application container starts up
   * the listener is called and retrieves the host/port from the container which
   * will subsequently be used as originator URI.
   *
   * @return
   */
  @Bean
  public ApplicationListener<EmbeddedServletContainerInitializedEvent> getPortDiscoveryBean(OnionClient client) {
    ApplicationListener<EmbeddedServletContainerInitializedEvent> listener = new
            ApplicationListener<EmbeddedServletContainerInitializedEvent>()
            {
              @Override
              public void onApplicationEvent(final EmbeddedServletContainerInitializedEvent embeddedServletContainerInitializedEvent) {
                EmbeddedServletContainer container = embeddedServletContainerInitializedEvent.getEmbeddedServletContainer();
                int port = container.getPort();
                logger.debug("servlet container initialized, port is {}", port);
                try {
                  String hostname = InetAddress.getLocalHost().getHostName();
                  URI originatorUri = URI.create("http://" + hostname + ":" + port);
                  client.setOriginatorUri(originatorUri);
                  logger.debug("originator uri: {}", originatorUri);
                } catch (UnknownHostException e) {
                  logger.warn("could not determine originator uri", e);
                }
              }
            };
    return listener;
  }

  @Bean
  public CommandLineRunner getOnionClientCommandLineRunner(){
    return new OnionClientCommandLineRunner();
  }


}
