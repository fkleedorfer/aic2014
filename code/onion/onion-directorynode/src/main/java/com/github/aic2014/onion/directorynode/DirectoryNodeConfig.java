package com.github.aic2014.onion.directorynode;

import com.github.aic2014.onion.directorynode.aws.AWSDirectoryNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.github.aic2014"})
@PropertySource("file:${ONION_CONF_DIR}/directorynode.properties")
@EnableScheduling
public class DirectoryNodeConfig {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${aws.enableautosetup}")
    private boolean useAWS = false;

    @Bean
    DirectoryNodeService getChainNodeService() {
        if (useAWS) {
            logger.info("Run AWSDirectoryNodeService");
            return new AWSDirectoryNodeService();
        }
        else {
            logger.info("Run InMemoryDirectoryService");
            return new InMemoryDirectoryService();
        }
    }

}