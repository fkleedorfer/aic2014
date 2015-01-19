package com.github.aic2014.onion.client;

import com.github.aic2014.onion.crypto.CryptoService;
import com.github.aic2014.onion.json.JsonUtils;
import com.github.aic2014.onion.model.ChainNodeInfo;
import com.github.aic2014.onion.model.Message;
import com.github.aic2014.onion.model.OnionStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.UUID;

public class OnionRoutedHttpRequest extends OnionRoutedRequest {

    public OnionRoutedHttpRequest(CryptoService cryptoService, URI directoryNodeURI, int chainErrorRetries) {
        super(cryptoService, directoryNodeURI, chainErrorRetries);
    }

    public String execute(String request) throws OnionRoutedRequestException {
        String response = "";

        for (int retry = 0; retry <= chainErrorRetries || chainErrorRetries < 1; retry++) {
            try {
                if (retry > 0)
                    logger.info("Fetching new chain and retrying request ({}/{})...", retry, chainErrorRetries);

                response = executeSingleRequest(request);
                break;

            } catch (OnionRoutedRequestException e) {

                // chain is defunct: fetch a new chain and retry
                if (e.getStatus() == OnionStatus.CHAIN_ERROR || e.getStatus() == OnionStatus.CHAIN_TIMEOUT) {
                    logger.info("Chain error {}: {}", e.getStatus(), e.getFailedNode());
                    continue;
                }

                throw e;
            }
        }

        return response;
    }

    private String executeSingleRequest(String request) throws OnionRoutedRequestException {

        fetchNewChain();
        Message msg = buildMessage(usedChain, usedChainID, request);
        logger.debug("sending this message: {}", msg);

        try {
            long start = System.nanoTime();
            ResponseEntity<Message> responseEntity = restTemplate.postForEntity(usedChain[0].getUri().toString() + "/request", msg, Message.class);
            Message responseMessage = responseEntity.getBody();
            String responseString = decryptResponse(responseMessage);
            roundTripTime = (System.nanoTime() - start) / 1000 / 1000;
            return responseString;

        } catch (RestClientException e) {
            OnionRoutedRequestException o = new OnionRoutedRequestException(OnionStatus.CHAIN_ERROR, e);
            o.setFailedNode(usedChain[0]);
            throw o;
        }
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

    private String decryptResponse(Message msg) throws OnionRoutedRequestException {
        int i;

        // decrypt payload, starting from first chain node to last
        for (i = 1; i < usedChain.length; i++) {
            if (msg.getStatus() != OnionStatus.OK) {
                break;
            }
            String payload = this.cryptoService.decrypt(msg.getPayload());
            msg = JsonUtils.fromJSON(payload);
        }

        if (msg.getStatus() == OnionStatus.OK) {
            return this.cryptoService.decrypt(msg.getPayload());
        }

        OnionRoutedRequestException e = new OnionRoutedRequestException(msg.getStatus());
        e.setFailedNode(usedChain[i - 1]);
        throw e;
    }
}
