package com.github.aic2014.onion.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy CryptoService that only applies base64 encoding of the specified string.
 */
public class DummyCryptoService implements CryptoService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String encrypt(String plaintext, String receiverPublicKey) throws CryptoServiceException {
        logger.debug("Encoding {} chars: {}", plaintext.length(), plaintext);
        return Base64Helper.encodeString(plaintext);
    }

    @Override
    public String decrypt(String ciphertext) throws CryptoServiceException {
        String plaintext = Base64Helper.decodeString(ciphertext);
        logger.debug("Decoded {} chars: {}", plaintext.length(), plaintext);
        return plaintext;
    }

    @Override
    public String getPublicKey() {
        return "";
    }
}

