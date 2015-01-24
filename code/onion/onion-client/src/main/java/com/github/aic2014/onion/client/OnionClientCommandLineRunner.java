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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class OnionClientCommandLineRunner implements CommandLineRunner
{
  private final Logger logger = LoggerFactory.getLogger(getClass());
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
      executor = Executors.newFixedThreadPool(5);
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
    OnionRoutedHttpRequest request = client.getHttpRequest();
    String response = request.execute(requestString);
    shell.writeLine(request.printUsedChain());
    return String.format("Response in %s ms: %s", request.getRoundTripTime(), response);
  }

  @Command
  public String bomb(int messageCount) throws Exception {
      long start = System.currentTimeMillis();
      final String requestString = buildRequestString();
      final CountDownLatch latch = new CountDownLatch(messageCount);
      final AtomicInteger requestSentCounter = new AtomicInteger(0);
      final AtomicInteger responseSuccessfulCounter = new AtomicInteger(0);
      final AtomicInteger responseFailedCounter = new AtomicInteger(0);
      Runnable sendTask = new Runnable(){
          @Override


          public void run() {
              try {
                Thread.sleep(300);
                requestSentCounter.addAndGet(1);
                reportStatus("Sent request... ");
                OnionRoutedHttpRequest request = client.getHttpRequest();
                String response = request.execute(requestString);
                responseSuccessfulCounter.addAndGet(1);
                reportStatus("Success! ");
              } catch (Throwable e) {
                  try {
                     responseFailedCounter.addAndGet(1);
                     reportStatus("Failure! ");
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  }
                  logger.debug("caught throwable when sending chain request from client", e);
              } finally {
                  latch.countDown();
              }
          }

        private void reportStatus(String message) throws IOException {
          shell.writeLine(String.format("%20s (successful: %s, failed: %s, %s still to go)", message,
                  responseSuccessfulCounter.get(), responseFailedCounter.get(),
                  messageCount - responseSuccessfulCounter.get() - responseFailedCounter.get()));
        }


      };
      shell.writeLine("-------------------------------");
      shell.writeLine(String.format("Sending %s messages...", messageCount));
      for (int i = 0; i < messageCount; i++) {
          executor.execute(sendTask);
      }
      latch.await();
      double time = (System.currentTimeMillis() - start) / 1000.0;
      return String.format("Done bombing. Attempted to send %s messages in %.2f seconds (%s successful, %s failed, %.2f messages per second)",
              messageCount, time, responseSuccessfulCounter.get(), responseFailedCounter.get(), messageCount/time);
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
