package com.github.aic2014.onion.crypto;

import com.github.aic2014.onion.crypto.cipher.AESCipher;
import com.github.aic2014.onion.crypto.cipher.Cipher;
import com.github.aic2014.onion.crypto.cipher.RSACipher;
import com.github.aic2014.onion.crypto.cipher.StringCipherAdapter;
import org.codehaus.jackson.map.ObjectMapper;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

/**
 * Message encryption service using a common hybrid mode of RSA and AES.
 * An AES key is generated, used to encrypt the payload, encrypted by itself using the RSA private key
 * and then passed along as part of the encrypted payload.
 */
public class RSAESCryptoService implements CryptoService {

    private KeyPair myKeyPair;

    public RSAESCryptoService() throws GeneralSecurityException {
        this(Generators.generateRSAKeyPair());
    }

    public RSAESCryptoService(KeyPair myKey) {
        this.myKeyPair = myKey;
    }

    @Override
    public String encrypt(String plaintext, PublicKey receiver) throws CryptoServiceException {
        try {
            EncryptedPayload ep = new EncryptedPayload();

            SecretKey key = Generators.generateAESKey();
            IvParameterSpec iv = Generators.generateIV();
            Cipher<String, byte[]> aes = new StringCipherAdapter<>(new AESCipher(key, iv));
            ep.payload = aes.encrypt(plaintext);
            ep.sessionIV = iv.getIV();

            Cipher<byte[], byte[]> rsa = new RSACipher(receiver);
            ep.sessionKey = rsa.encrypt(key.getEncoded());

            return ep.toJSON();

        } catch (GeneralSecurityException e) {
            throw new CryptoServiceException(e);
        }
    }

    @Override
    public String decrypt(String ciphertext) throws CryptoServiceException {
        try {
            EncryptedPayload ep = EncryptedPayload.fromJSON(ciphertext);

            Cipher<byte[], byte[]> rsa = new RSACipher(myKeyPair);
            SecretKey key = new SecretKeySpec(rsa.decrypt(ep.sessionKey), "AES");

            IvParameterSpec iv = new IvParameterSpec(ep.sessionIV);
            Cipher<String, byte[]> aes = new StringCipherAdapter<>(new AESCipher(key, iv));
            return aes.decrypt(ep.payload);

        } catch (GeneralSecurityException e) {
            throw new CryptoServiceException(e);
        }
    }

    @Override
    public PublicKey getPublicKey() {
        return myKeyPair.getPublic();
    }

    /**
     * Internal format for encrypted payload and accompanying session parameters.
     */
    static class EncryptedPayload {

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
}

