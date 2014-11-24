package com.github.aic2014.onion.chainnode;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.exception.OnionRoutingException;
import com.github.aic2014.onion.model.Message;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.io.*;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Service responsible for asynchonously executing the
 * http requests (for each hop in the chain and for the exit)
 * and for feeding the http response back into the chain.
 */
@Service
public class AsyncRequestService {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  @Autowired
  private ResponseInfoService responseInfoService;
  @Autowired
  private CryptoService cryptoService;

  private RestTemplate restTemplate = new RestTemplate();

  private DefaultHttpRequestParserFactory httpRequestParserFactory = new DefaultHttpRequestParserFactory();

  @Async
  public void sendExitRequestAndTunnelResponse(String request, UUID chainId){
    logger.debug("sending tunneled http request {}", request);
    //TODO: execute http request, convert response into a string
      String response = null;
      try {
          response = sendRequestSynchronously(request);
      } catch (Exception e) {
          //TODO: log to debug and send error message back to originator
          logger.warn("caught exception:", e);
      }

      //String response = "Exit node received this request: '" + request + "', currently not sending the request anywhere";
    Message responseMessage = new Message();
    ResponseInfo responseInfo = responseInfoService.getAndDeleteResponseInfo(chainId);
    if (responseInfo == null){
      throw new IllegalArgumentException("could not obtain ResponseInfo for id " + chainId);
    }
    responseMessage.setChainId(chainId);
    responseMessage.setPublicKey(responseInfo.getPublicKey());
    responseMessage.setPayload(this.cryptoService.encrypt(response, responseInfo.getPublicKey()));
    logger.debug("sending this response message back through the chain: {}", responseMessage);
    this.restTemplate.put(responseInfo.getSenderOfRequest() + "/response", responseMessage);
  }

    /**
     * Sends the specified http request synchronously.
     * @param request the full string of the http request to send.
     * @return
     * @throws IOException
     * @throws HttpException
     */
    private String sendRequestSynchronously(String request) throws IOException, HttpException {
        //initialize stuff we need for http request handling
        HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
        MessageConstraints constraints = MessageConstraints.DEFAULT;

        SessionInputBufferImpl sessionInputBuffer = new SessionInputBufferImpl(metrics, 255);
        HttpMessageParser parser = httpRequestParserFactory.create(sessionInputBuffer, constraints);
        ByteArrayInputStream in = new ByteArrayInputStream(request.getBytes());
        sessionInputBuffer.bind(in);
        //parse out a HttpMessage
        HttpMessage httpMessage = parser.parse();
        if (!httpMessage.containsHeader("Host")){
            throw new OnionRoutingException("No 'Host' header found in the http request");
        }
        HttpRequest httpRequest = null;
        if (!(httpMessage instanceof HttpRequest)){
            throw new OnionRoutingException("not an http request");
        }
        httpRequest = (HttpRequest) httpMessage;
        Header[] hostHeaders = httpRequest.getHeaders("Host");
        String hostHeader = hostHeaders[0].getValue();
        HttpHost httpHost = null;
        int colonIdx = hostHeader.indexOf(':');
        if (colonIdx > -1){
            httpHost = new HttpHost(hostHeader.substring(0, colonIdx), Integer.parseInt(hostHeader.substring(colonIdx+1)));
        } else {
            httpHost = new HttpHost(hostHeader);
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpHost, httpRequest);
        SessionOutputBufferImpl sessionOutputBuffer = new SessionOutputBufferImpl(metrics, 255);
        HttpMessageWriter<HttpResponse> httpResponseWriter = new DefaultHttpResponseWriter(sessionOutputBuffer);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sessionOutputBuffer.bind(out);
        httpResponseWriter.write(response);
        sessionOutputBuffer.flush();
        return new String(out.toByteArray());
    }

    @Async
  public void sendChainRequest(Message msg) {
    logger.debug("sending chain request message: {}", msg);
    restTemplate.put(msg.getRecipient()+"/request", msg);
  }

  public void sendChainResponse(Message msg) {
    ResponseInfo responseInfo = this.responseInfoService.getAndDeleteResponseInfo(msg.getChainId());
    if (responseInfo == null){
      throw new IllegalStateException("cannot retrieve ResponseInfo");
    }
    logger.debug("/response: sending message: {}", msg);
    restTemplate.put(responseInfo.getSenderOfRequest() + "/response", msg);
  }
}