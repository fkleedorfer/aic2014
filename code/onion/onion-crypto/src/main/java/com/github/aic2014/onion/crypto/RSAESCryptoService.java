package com.github.aic2014.onion.crypto;

import com.github.aic2014.onion.crypto.cipher.AESCipher;
import com.github.aic2014.onion.crypto.cipher.Cipher;
import com.github.aic2014.onion.crypto.cipher.RSACipher;
import com.github.aic2014.onion.crypto.cipher.StringCipherAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

/**
 * Message encryption service using a common hybrid mode of RSA and AES.
 * An AES key is generated, used to encrypt the payload, encrypted by itself using the RSA private key
 * and then passed along as part of the encrypted payload.
 */
public class RSAESCryptoService implements CryptoService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private KeyPair myKeyPair;

    public RSAESCryptoService() throws GeneralSecurityException {
        this(Generators.generateRSAKeyPair());
    }

    public RSAESCryptoService(KeyPair myKey) {
        this.myKeyPair = myKey;
    }

    @Override
    public String encrypt(String plaintext, String receiverPublicKey) throws CryptoServiceException {
        try {
            PublicKey pk = new PublicKeyString(receiverPublicKey).getPublicKey();
            EncryptedPayload ep = encrypt(plaintext, pk);
            return ep.toJSON();
        } catch (GeneralSecurityException e) {
            throw new CryptoServiceException(e);
        }
    }

    private EncryptedPayload encrypt(String plaintext, PublicKey receiverPublicKey) throws CryptoServiceException {
        try {
            logger.debug("Encrypting {} chars: {}", plaintext.length(), plaintext);
            SecretKey key = Generators.generateAESKey();
            IvParameterSpec iv = Generators.generateIV();
            Cipher<String, byte[]> aes = new StringCipherAdapter<>(new AESCipher(key, iv));

            EncryptedPayload ep = new EncryptedPayload();
            ep.payload = aes.encrypt(plaintext);
            ep.sessionIV = iv.getIV();

            Cipher<byte[], byte[]> rsa = new RSACipher(receiverPublicKey);
            ep.sessionKey = rsa.encrypt(key.getEncoded());

            return ep;

        } catch (GeneralSecurityException e) {
            throw new CryptoServiceException(e);
        }
    }

    @Override
    public String decrypt(String ciphertext) throws CryptoServiceException {
        EncryptedPayload ep = EncryptedPayload.fromJSON(ciphertext);
        return decrypt(ep);
    }

    private String decrypt(EncryptedPayload ep) throws CryptoServiceException {
        try {
            Cipher<byte[], byte[]> rsa = new RSACipher(myKeyPair);
            SecretKey key = new SecretKeySpec(rsa.decrypt(ep.sessionKey), "AES");

            IvParameterSpec iv = new IvParameterSpec(ep.sessionIV);
            Cipher<String, byte[]> aes = new StringCipherAdapter<>(new AESCipher(key, iv));
            String plaintext = aes.decrypt(ep.payload);

            logger.debug("Decrypted {} chars: {}", plaintext.length(), plaintext);
            return plaintext;

        } catch (GeneralSecurityException e) {
            throw new CryptoServiceException(e);
        }
    }

    @Override
    public String getPublicKey() {
        PublicKeyString pk = new PublicKeyString(myKeyPair.getPublic());
        return pk.toString();
    }

}

