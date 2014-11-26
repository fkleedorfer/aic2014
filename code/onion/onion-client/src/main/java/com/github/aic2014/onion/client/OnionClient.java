package com.github.aic2014.onion.client;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.json.JsonUtils;
import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.Message;
import com.github.aic2014.onion.model.OnionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Client that accesses the directory node for obtaining a chain and then
 * sends an onion-routed request through the chain.
 */
public class OnionClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private URI directoryNodeUri;
    private URI quoteServerUri;
    private URI originatorUri;
    @Autowired
    private CryptoService cryptoService;

    private RestTemplate restTemplate = new RestTemplate();
    private Map<UUID, Future<String>> pendingResponses = new HashMap<UUID, Future<String>>();

    public OnionClient() {
    }

    private ChainNodeInfo[] getChain() {
        String requestUri = directoryNodeUri.toString() + "/getChain";
        logger.info("requesting: {}", requestUri);
        ResponseEntity<ChainNodeInfo[]> newChainResult = restTemplate.getForEntity(requestUri, ChainNodeInfo[].class);
        ChainNodeInfo[] newChain = newChainResult.getBody();
        logger.debug("obtained new chain: {}", newChain);
        return newChain;
    }

    public String executeOnionRoutedHttpRequest(String request) {
        //get the chain for the request
        ChainNodeInfo[] chain = getChain();
        logger.info("obtained chain {}", Arrays.toString(chain));
        UUID chainId = UUID.randomUUID();
        Message msg = buildMessage(chain, chainId, request);
        logger.debug("sending this message: {}", msg);
        ResponseEntity<Message> responseEntity = restTemplate.postForEntity(chain[0].getUri().toString() + "/request", msg, Message.class);
        //TODO: handle error
        Message responeMessage = responseEntity.getBody();
        String responseString = decryptResponse(responeMessage, chain.length);
        return responseString;
    }

    private Message buildMessage(final ChainNodeInfo[] chain, UUID chainId, final String payload) {
        logger.debug("building message for chain {} and payload {}", chain, payload);

        String lastPayload = payload;
        Message msg = null;
        //from last call to first call, create message object, serialize it and
        //add it encrypted as payload to the one before.
        //start by building the outermost
        for (int idx = chain.length - 1; idx >= 0; idx--) {
            msg = new Message();
            msg.setChainId(chainId);
            msg.setRecipient(chain[idx].getUri());
            if (idx > 0) {
                msg.setSender(chain[idx - 1].getUri());
            } else {
                //for the outermost message, the client is the sender
                msg.setSender(this.originatorUri);
            }
            msg.setHopsToGo(chain.length - 1 - idx);
            msg.setPublicKey(this.cryptoService.getPublicKey());
            msg.setPayload(this.cryptoService.encrypt(lastPayload, chain[idx].getPublicKey()));
            logger.debug("message for chain step {}: {}", idx, msg);
            //now, convert the newly built message to a payload for the next message
            lastPayload = JsonUtils.toJSON(msg);
        }
        return msg;
    }


    private String decryptResponse(Message msg, int chainLength) {
        for (int i = 1; i < chainLength; i++) {
            if (msg.getStatus() != OnionStatus.OK) {
                break;
            }
            String payload = this.cryptoService.decrypt(msg.getPayload());
            msg = JsonUtils.fromJSON(payload);
        }
        if (msg.getStatus() == OnionStatus.OK) {
            return this.cryptoService.decrypt(msg.getPayload());
        } else {
            StringBuilder errmsg = new StringBuilder();
            errmsg.append("Failed to execute onion routing request: ")
                    .append(msg.toString());
            return errmsg.toString();
        }
    }


    public void setDirectoryNodeUri(final URI directoryNodeUri) {
        this.directoryNodeUri = directoryNodeUri;
    }

    public void setQuoteServerUri(URI quoteServerUri) {
        this.quoteServerUri = quoteServerUri;
    }

    public void setOriginatorUri(URI originatorUri) {
        this.originatorUri = originatorUri;
    }
}
