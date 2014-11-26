package com.github.aic2014.onion.chainnode;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.exception.OnionRoutingException;
import com.github.aic2014.onion.exception.OnionRoutingRequestException;
import com.github.aic2014.onion.exception.OnionRoutingTargetRequestException;
import com.github.aic2014.onion.model.Message;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.io.*;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.UnknownHostException;

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
    @Value("${outgoingRequest.connectTimeout}")
    private int outgoingRequestConnectTimeout;
    @Value("${outgoingRequest.socketTimeout}")
    private int outgoingRequestSocketTimeout;

    @Async
    public ListenableFuture<String> sendChainRequest(Message msg) {
        try {
            logger.debug("sending chain request message: {}", msg);
            ResponseEntity<String> msgEntity = restTemplate.postForEntity(msg.getRecipient() + "/request", msg, String.class);
            return new AsyncResult<String>(msgEntity.getBody());
        } catch (Exception e) {
            //assume the receiver is misbehaving
            throw new OnionRoutingRequestException(e, msg.getRecipient());
        }
    }


    @Async
    public ListenableFuture<String> sendExitRequestAndTunnelResponse(URI sender, String request) throws OnionRoutingException {
        String response = null;
        logger.debug("sending tunneled http request {}", request);
        response = sendRequestSynchronously(sender, request);
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
    private String sendRequestSynchronously(URI sender, String request) throws OnionRoutingRequestException {
        String responseString = null;
        try {
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
            if (!(httpMessage instanceof HttpRequest)) {
                throw new OnionRoutingException("not an http request");
            }
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(outgoingRequestSocketTimeout)
                    .setConnectTimeout(outgoingRequestConnectTimeout)
                    .build();
            RequestBuilder builder = RequestBuilder.copy( (HttpRequest)httpMessage);
            builder.setUri(((HttpRequest)httpMessage).getRequestLine().getUri()); //this is a workaround for a bug in RequestBuilder.copy()
            builder.setConfig(requestConfig);
            HttpUriRequest httpRequest =  builder.build();
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
            responseString = new String(out.toByteArray());
        } catch (HttpException e) {
            // assume that the target is misbehaving
            throw new OnionRoutingTargetRequestException(e);
        } catch (ClientProtocolException e) {
            // assume that the target is misbehaving
            throw new OnionRoutingTargetRequestException(e);
        } catch (IOException e) {
            //TODO: refactor status code/error propagation mechanism to propagate more
            //detailed info back to originator
            if (e instanceof InterruptedIOException) {
                // Timeout
                throw new OnionRoutingTargetRequestException(e);
            }
            if (e instanceof UnknownHostException) {
                // Unknown host
                throw new OnionRoutingTargetRequestException(e);
            }
            if (e instanceof ConnectTimeoutException) {
                // Connection refused
                throw new OnionRoutingTargetRequestException(e);
            }
            if (e instanceof SSLException) {
                // SSL handshake exception
                throw new OnionRoutingTargetRequestException(e);
            } else {
                //we may be misbehaving
                throw new OnionRoutingRequestException(e, sender);
            }
        } catch (Exception e){
            // assume that we are misbehaving
            throw new OnionRoutingRequestException(e, sender);
        }
        return responseString;
    }

    public void setOutgoingRequestConnectTimeout(int outgoingRequestConnectTimeout) {
        this.outgoingRequestConnectTimeout = outgoingRequestConnectTimeout;
    }

    public void setOutgoingRequestSocketTimeout(int outgoingRequestSocketTimeout) {
        this.outgoingRequestSocketTimeout = outgoingRequestSocketTimeout;
    }
}