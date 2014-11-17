package com.github.aic2014.onion.crypto;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Internal format for encrypted payload and accompanying session parameters.
 */
class EncryptedPayload {

    // RSA encrypted AES session key
    public byte[] sessionKey;
    // IV for AES
    public byte[] sessionIV;
    // AES encrypted payload
    public byte[] payload;

    public static EncryptedPayload fromJSON(String ep) throws CryptoServiceException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(ep, EncryptedPayload.class);
        } catch (IOException e) {
            throw new CryptoServiceException("JSON mapping", e);
        }
    }

    public String toJSON() throws CryptoServiceException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new CryptoServiceException("JSON mapping", e);
        }
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
