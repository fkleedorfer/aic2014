package com.github.aic2014.onion.crypto;

/**
 * Service for encrypting/decrypting a message.
 */
public interface CryptoService {

    /**
     * Encrypts the specified plaintext and returns a Base64 encoded ciphertext.
     * @param plaintext Plaintext message to send
     * @param receiverPublicKey Public key component of the intended message receiver
     * @return Base64 encoded ciphertext
     */
    public String encrypt(String plaintext, String receiverPublicKey) throws CryptoServiceException;

    /**
     * Decrypts the specified Base64 encoded ciphertext and returns the plaintext.
     * @param ciphertext Base64 encoded ciphertext to decrypt
     * @return Plaintext message
     */
    public String decrypt(String ciphertext) throws CryptoServiceException;

    /**
     * @return Gets the public key to encrypt messages for this CryptoService.
     */
    public String getPublicKey();
}
