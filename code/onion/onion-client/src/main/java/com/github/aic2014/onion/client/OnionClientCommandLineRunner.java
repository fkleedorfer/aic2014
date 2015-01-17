package com.github.aic2014.onion.client;

import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.shell.Command;
import com.github.aic2014.onion.shell.Shell;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.io.DefaultHttpRequestWriter;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionOutputBufferImpl;
import org.apache.http.io.HttpMessageWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
      executor = Executors.newFixedThreadPool(20);
      executor.execute(shell);
      printUsage();
  }

  @Command(value = "help")
  public void printUsage() throws Exception {
      shell.writeLine("Welcome to the Onion Routing Demo! :)");
      shell.writeLine("This are your commands:");
      shell.writeLine("!send ... sends a request and prints the response to the Console");
      shell.writeLine("!bomb N ... sends N requests multiple parallel threads");
      shell.writeLine("!help ... shows this usage notice");
      shell.writeLine("!exit ... stops the Client");
  }

  @Command
  public String send() throws Exception {

      String requestString = buildRequestString();
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
  public String bomb(int messageCount) throws Exception {
      final String requestString = buildRequestString();
      final CountDownLatch latch = new CountDownLatch(messageCount);
      Runnable sendTask = new Runnable(){
          @Override
          public void run() {
              try {
                  ChainNodeInfo[] chain = client.getChain();
                  String response = client.executeOnionRoutedHttpRequest(requestString, chain);
              } catch (Exception e) {
                  e.printStackTrace();
              } finally {
                  latch.countDown();
              }
          }
      };
      shell.writeLine("-------------------------------");
      shell.writeLine(String.format("Sending %s messages...", messageCount));
      for (int i = 0; i < messageCount; i++) {
          executor.execute(sendTask);
      }
      latch.await();
      return "Received all responses";
  }

    private String buildRequestString() throws java.io.IOException, org.apache.http.HttpException {
        HttpGet request = new HttpGet(quoteServerUri+"/quote");
        request.addHeader("Host", quoteServerHostnamePort);
        HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
        SessionOutputBufferImpl sessionOutputBuffer = new SessionOutputBufferImpl(metrics, 255);
        HttpMessageWriter<HttpRequest> httpRequestWriter = new DefaultHttpRequestWriter(sessionOutputBuffer);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sessionOutputBuffer.bind(out);
        httpRequestWriter.write(request);
        sessionOutputBuffer.flush();
        return new String(out.toByteArray());
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
