package com.github.aic2014.onion.client;

import com.github.aic2014.onion.model.RoutingInfo;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.github.aic2014.onion.client.OnionClientCommandLineRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller that provides routing information for debugging/demo purposes.
 */
@Controller
public class OnionClientAppController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static ExecutorService executor = executor = Executors.newCachedThreadPool();

    @Autowired
    private OnionClient client;

    @Value("${quoteserver.baseUri}")
    private String quoteServerUri;

    @Value("${quoteserver.hostnamePort}")
    private String quoteServerHostnamePort;

    @RequestMapping(value = "/sendBomb", method = RequestMethod.GET)
    public ResponseEntity<ResponseText> sendBomb() throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        Calendar cal = Calendar.getInstance();

        final String[] res = {""};
        int messageCount = 20;

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
                res[0] = dateFormat.format(cal.getTime()) + " - " + String.format("%20s (successful: %s, failed: %s, %s still to go)", message,
                        responseSuccessfulCounter.get(), responseFailedCounter.get(),
                        messageCount - responseSuccessfulCounter.get() - responseFailedCounter.get()) + "\n";
            }
        };
        res[0] = res[0] + "--------------------------------------------------------------------------------------------\n" + String.format("Sending %s messages...", messageCount) + "\n";
        for (int i = 0; i < messageCount; i++) {
            executor.execute(sendTask);
        }
        latch.await();
        double time = (System.currentTimeMillis() - start) / 1000.0;
        res[0] = res[0] + dateFormat.format(cal.getTime()) + " - " + String.format("Done bombing. Attempted to send %s messages in %.2f seconds (%s successful, %s failed, %.2f messages per second)",
                messageCount, time, responseSuccessfulCounter.get(), responseFailedCounter.get(), messageCount/time) + "\n";
        res[0] = res [0] + "--------------------------------------------------------------------------------------------\n";
        return new ResponseEntity<ResponseText>(new ResponseText(res[0]), HttpStatus.OK);
    }

    @RequestMapping(value="/sendRequest", method = RequestMethod.GET)
    public ResponseEntity<ResponseText> sendRequest() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        Calendar cal = Calendar.getInstance();
        String res = "";
        try {
            String requestString = dateFormat.format(cal.getTime()) + " - " + buildRequestString();
            res = dateFormat.format(cal.getTime()) + " - Sending Request: " + requestString + "\n";
            OnionRoutedHttpRequest request = client.getHttpRequest();
            String response = request.execute(requestString);
            res = res + dateFormat.format(cal.getTime()) + " - " + request.printUsedChain() + "\n";
            res = res + dateFormat.format(cal.getTime()) + " - " + String.format("Response in %s ms: %s", request.getRoundTripTime(), response) + "\n";
            res = res + "--------------------------------------------------------------------------------------------\n";
            return new ResponseEntity<ResponseText>(new ResponseText(res), HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            res = dateFormat.format(cal.getTime()) + " - Request Sending failed!\n"+
                    "--------------------------------------------------------------------------------------------\n";
            return new ResponseEntity<ResponseText>(new ResponseText(res), HttpStatus.OK);
        }
    }

    @RequestMapping(value="/sendHelp", method = RequestMethod.GET)
    public ResponseEntity<ResponseText> sendHelp(){

        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        Calendar cal = Calendar.getInstance();

        String a = dateFormat.format(cal.getTime()) + " - Welcome to the Onion Routing Demo! :)\n" +
                dateFormat.format(cal.getTime()) + " - This are your commands:\n" +
                dateFormat.format(cal.getTime()) + " - send ... sends a request and prints the response to the Console\n" +
                dateFormat.format(cal.getTime()) + " - bomb N ... sends N requests multiple parallel threads\n" +
                dateFormat.format(cal.getTime()) + " - help ... shows this usage notice\n" +
                dateFormat.format(cal.getTime()) + " - exit ... stops the Client\n" +
                "--------------------------------------------------------------------------------------------\n";
        return new ResponseEntity<ResponseText>(new ResponseText(a), HttpStatus.OK);
    }

    @RequestMapping(value="/sendExit", method = RequestMethod.GET)
    public ResponseEntity<ResponseText> sendExit(){
        executor.shutdownNow();
        System.exit(0);
        return new ResponseEntity<ResponseText>(new ResponseText(""), HttpStatus.OK);
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
}