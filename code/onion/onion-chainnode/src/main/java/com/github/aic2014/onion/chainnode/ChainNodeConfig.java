package com.github.aic2014.onion.chainnode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/chainnode.properties")
public class ChainNodeConfig
{
  private final Logger logger = LoggerFactory.getLogger(getClass());
  /**
   * Bean that registers an application listener. When the application container starts up
   * the listener is called and retrieves the host/port from the container and
   * registers with the directory node.
   *
   * @return
   */
  @Bean
  public ApplicationListener<EmbeddedServletContainerInitializedEvent> getPortDiscoveryBean() {
    ApplicationListener<EmbeddedServletContainerInitializedEvent> listener = new
      ApplicationListener<EmbeddedServletContainerInitializedEvent>()
      {
        @Override
        public void onApplicationEvent(final EmbeddedServletContainerInitializedEvent embeddedServletContainerInitializedEvent) {
          EmbeddedServletContainer container = embeddedServletContainerInitializedEvent.getEmbeddedServletContainer();
          int port = container.getPort();
          logger.debug("servlet container initialized, port is {}", port);

        }
      };
    return listener;
  }



}
