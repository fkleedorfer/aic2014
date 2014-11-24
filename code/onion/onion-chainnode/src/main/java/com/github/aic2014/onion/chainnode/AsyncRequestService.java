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
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service responsible for asynchonously executing the
 * http requests (for each hop in the chain and for the exit)
 * and for feeding the http response back into the chain.
 */
@Service
public class AsyncRequestService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private CryptoService cryptoService;

    private RestTemplate restTemplate = new RestTemplate();

    private DefaultHttpRequestParserFactory httpRequestParserFactory = new DefaultHttpRequestParserFactory();

    @Async
    public ListenableFuture<String> sendChainRequest(Message msg) {
        logger.debug("sending chain request message: {}", msg);
        ResponseEntity<String> msgEntity = restTemplate.postForEntity(msg.getRecipient() + "/request", msg, String.class);
        //TODO: handle errors
        return new AsyncResult<String>(msgEntity.getBody());
    }


    @Async
    public ListenableFuture<String> sendExitRequestAndTunnelResponse(String request) {
        logger.debug("sending tunneled http request {}", request);
        SettableListenableFuture<Message> msgFuture = new SettableListenableFuture<>();
        String response = null;
        try {
            response = sendRequestSynchronously(request);
        } catch (Exception e) {
            //TODO: log to debug and send error message back to originator
            logger.warn("caught exception:", e);
        }
        return new AsyncResult<String>(response);
    }

    /**
     * Sends the specified http request synchronously.
     *
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
        if (!httpMessage.containsHeader("Host")) {
            throw new OnionRoutingException("No 'Host' header found in the http request");
        }
        HttpRequest httpRequest = null;
        if (!(httpMessage instanceof HttpRequest)) {
            throw new OnionRoutingException("not an http request");
        }
        httpRequest = (HttpRequest) httpMessage;
        Header[] hostHeaders = httpRequest.getHeaders("Host");
        String hostHeader = hostHeaders[0].getValue();
        HttpHost httpHost = null;
        int colonIdx = hostHeader.indexOf(':');
        if (colonIdx > -1) {
            httpHost = new HttpHost(hostHeader.substring(0, colonIdx), Integer.parseInt(hostHeader.substring(colonIdx + 1)));
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


}