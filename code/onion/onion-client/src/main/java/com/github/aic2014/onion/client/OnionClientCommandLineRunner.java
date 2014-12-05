package com.github.aic2014.onion.client;

import com.github.aic2014.onion.model.ChainNodeInfo;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.io.DefaultHttpRequestWriter;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionOutputBufferImpl;
import org.apache.http.io.HttpMessageWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.aic2014.onion.shell.*;
import java.io.ByteArrayOutputStream;

public class OnionClientCommandLineRunner implements CommandLineRunner
{

  private Shell shell;
  private static ExecutorService executor;

  @Autowired
  private OnionClient client;

  @Value("${quoteserver.baseUri}")
  private String quoteServerUri;

  @Value("${quoteserver.hostnamePort}")
  private String quoteServerHostnamePort;

  @Override
  public void run(final String... strings) throws Exception {
      shell = new Shell("Client", System.in, System.out);
      shell.register(this);
      executor = Executors.newFixedThreadPool(1);
      executor.execute(shell);
      printUsage();
  }

  public void printUsage() throws Exception {
      shell.writeLine("Welcome to the Onion Routing Demo! :)");
      shell.writeLine("This are your commands:");
      shell.writeLine("!send ... sends a request and prints the response to the Console");
      shell.writeLine("!exit ... stops the Client");
  }

  @Command
  public String send() throws Exception {

      HttpGet request = new HttpGet(quoteServerUri+"/quote");
      request.addHeader("Host", quoteServerHostnamePort);
      HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
      SessionOutputBufferImpl sessionOutputBuffer = new SessionOutputBufferImpl(metrics, 255);
      HttpMessageWriter<HttpRequest> httpRequestWriter = new DefaultHttpRequestWriter(sessionOutputBuffer);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      sessionOutputBuffer.bind(out);
      httpRequestWriter.write(request);
      sessionOutputBuffer.flush();
      String requestString = new String(out.toByteArray());
      shell.writeLine("Sending Request: " + requestString);

      ChainNodeInfo[] chain = client.getChain();
      shell.writeLine("-------------------------------");

      shell.writeLine("Used Chain:");
      for(int i = 0; i < 3; i++) {
          shell.writeLine("ChainNode"+i+": " + chain[i].getPublicIP() + ":" + chain[i].getPort());
      }
      shell.writeLine("-------------------------------");

      long start = System.nanoTime();
      // print
      String response = client.executeOnionRoutedHttpRequest(requestString, chain);

      return String.format("Response in %s msec: %s", (System.nanoTime() - start) / 1000 / 1000, response);
  }

  @Command
  public String exit() throws Exception {
      shell.writeLine("Stopping the client ...");
      shell.writeLine("Client has been stopped!");
      executor.shutdownNow();
      System.exit(0);
      return "";
  }
}
