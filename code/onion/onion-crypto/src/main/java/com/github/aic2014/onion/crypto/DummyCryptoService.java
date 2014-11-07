package com.github.aic2014.onion.crypto;

import java.util.Base64;

/**
 * Dummy CryptoService that only applies base64 encoding of the specified string.
 */
public class DummyCryptoService implements CryptoService {
    @Override
    public String encrypt(String plaintext) {
        byte[] message = plaintext.getBytes();
        return Base64.getEncoder().encodeToString(message);
    }

    @Override
    public String decrypt(String ciphertext) {
        return new String(Base64.getDecoder().decode(ciphertext));
    }
}
