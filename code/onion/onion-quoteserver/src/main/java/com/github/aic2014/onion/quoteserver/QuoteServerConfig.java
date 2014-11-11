package com.github.aic2014.onion.quoteserver;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/quoteserver.properties")
public class QuoteServerConfig
{
  //nothing to be done here for now, all is magically configured through
  //the annotations


}
