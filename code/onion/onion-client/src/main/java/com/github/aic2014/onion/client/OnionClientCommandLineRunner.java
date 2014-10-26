package com.github.aic2014.onion.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public class OnionClientCommandLineRunner implements CommandLineRunner
{

  @Autowired
  private OnionClient client;

  @Override
  public void run(final String... strings) throws Exception {
    client.sendRequest();
  }
}
