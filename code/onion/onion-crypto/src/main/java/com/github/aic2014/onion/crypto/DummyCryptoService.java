package com.github.aic2014.onion.crypto;

import java.security.PublicKey;

/**
 * Dummy CryptoService that only applies base64 encoding of the specified string.
 */
public class DummyCryptoService implements CryptoService {
    @Override
    public String encrypt(String plaintext, PublicKey receiver) throws CryptoServiceException {
        return Base64Helper.encodeString(plaintext);
    }

    @Override
    public String decrypt(String ciphertext) throws CryptoServiceException {
        return Base64Helper.decodeString(ciphertext);
    }

    @Override
    public PublicKey getPublicKey() {
        return null;
    }
}

