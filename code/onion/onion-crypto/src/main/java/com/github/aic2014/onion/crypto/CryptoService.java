package com.github.aic2014.onion.crypto;

/**
 * Service for encrypting/decrypting a message.
 */
public interface CryptoService {

    /**
     * Encrypts the specified plaintext and returns a Base64 encoded ciphertext.
     * TODO: adapt interface so it uses a key for encryption - this is just a bootstrapping version
     *
     *
     *
     * @param plaintext
     * @return
     */
    public String encrypt(String plaintext);


    /**
     * Decrypts the specified Base64 encoded ciphertext and returns the plaintext.
     * TODO: adapt interface so it uses a key for encryption - this is just a bootstrapping version
     *
     * @param cyphertext
     * @return
     */
    public String decrypt(String cyphertext);

}
