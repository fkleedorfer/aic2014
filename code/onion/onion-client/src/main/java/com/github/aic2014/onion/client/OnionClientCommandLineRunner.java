package com.github.aic2014.onion.client;

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
import java.util.concurrent.Future;

public class OnionClientCommandLineRunner implements CommandLineRunner
{

  @Autowired
  private OnionClient client;

  @Value("${quoteserver.baseUri}")
  private String quoteServerUri;

  @Override
  public void run(final String... strings) throws Exception {
      HttpGet request = new HttpGet("localhost:20140/quote");
      request.addHeader("Host", "localhost:20140");
      HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
      SessionOutputBufferImpl sessionOutputBuffer = new SessionOutputBufferImpl(metrics, 255);
      HttpMessageWriter<HttpRequest> httpRequestWriter = new DefaultHttpRequestWriter(sessionOutputBuffer);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      sessionOutputBuffer.bind(out);
      httpRequestWriter.write(request);
      sessionOutputBuffer.flush();
      String requestString = new String(out.toByteArray());
      System.out.println("sending request:\n" + requestString);
      Future<String> response = client.executeOnionRoutedHttpRequest(requestString);
      System.out.println("Response: " + response.get());
  }
}
