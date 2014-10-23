package com.github.aic2014.onion.chainnode;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/chainnode.properties")
public class ChainNodeConfig
{
  //TODO: add bean that registers with the directory node upon startup

  //TODO: configure server.port to use a random value (at least in local development setups)
}
