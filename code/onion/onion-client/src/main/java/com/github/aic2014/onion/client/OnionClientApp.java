package com.github.aic2014.onion.client;


import com.github.aic2014.onion.shell.Command;
import com.github.aic2014.onion.shell.Shell;
import org.springframework.boot.SpringApplication;

import java.io.IOException;

public class OnionClientApp
{

  public static void main(String... args) {
        SpringApplication.run(OnionClientConfig.class, args);
  }
}

