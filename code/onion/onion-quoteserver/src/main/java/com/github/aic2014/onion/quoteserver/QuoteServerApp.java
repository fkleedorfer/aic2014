package com.github.aic2014.onion.quoteserver;
import org.springframework.boot.SpringApplication;
/**
 * Spring boot application that starts the quote server.
 */
public class QuoteServerApp
{
    public static void main(String... args) {
        SpringApplication.run(QuoteServerConfig.class, args);
    }
}