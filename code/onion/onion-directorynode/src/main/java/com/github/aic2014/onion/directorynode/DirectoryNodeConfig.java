package com.github.aic2014.onion.directorynode;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/directorynode.properties")
public class DirectoryNodeConfig
{
  @Bean
  ChainNodeService getChainNodeService(){
    return new InMemoryChainNodeService();
  }
}
