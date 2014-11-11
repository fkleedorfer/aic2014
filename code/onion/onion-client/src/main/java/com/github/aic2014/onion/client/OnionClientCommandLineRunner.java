package com.github.aic2014.onion.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.Future;

public class OnionClientCommandLineRunner implements CommandLineRunner
{

  @Autowired
  private OnionClient client;

  @Override
  public void run(final String... strings) throws Exception {
      Future<String> response = client.executeOnionRoutedHttpRequest(
              "GET /quote HTTP/1.1\n"+
              "Host : http://localhost:20140\n\n"
      );
  }
}
