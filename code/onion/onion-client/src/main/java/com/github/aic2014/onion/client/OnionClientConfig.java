package com.github.aic2014.onion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.URI;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/client.properties")
public class OnionClientConfig
{

  @Value("${directorynode.baseUri}")
  private String directoryNodeBaseUri;


  @Bean
  public OnionClient getOnionClient(){
    OnionClient client = new OnionClient();
    client.setDirectoryNodeUri(URI.create(directoryNodeBaseUri));
    return client;
  }

  @Bean
  public CommandLineRunner getOnionClientCommandLineRunner(){
    return new OnionClientCommandLineRunner();
  }
}
