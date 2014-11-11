package com.github.aic2014.onion.crypto;

import com.github.aic2014.onion.crypto.cipher.AESCipher;
import com.github.aic2014.onion.crypto.cipher.Cipher;
import com.github.aic2014.onion.crypto.cipher.RSACipher;
import com.github.aic2014.onion.crypto.cipher.StringCipherAdapter;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            return ep.toString();

        } catch (GeneralSecurityException e) {
            throw new CryptoServiceException(e);
        }
    }

    @Override
    public String decrypt(String ciphertext) throws CryptoServiceException {
        try {
            EncryptedPayload ep = new EncryptedPayload(ciphertext);
            String plaintext;

            Cipher<byte[], byte[]> rsa = new RSACipher(myKeyPair);
            SecretKey key = new SecretKeySpec(rsa.decrypt(ep.sessionKey), "AES");

            IvParameterSpec iv = new IvParameterSpec(ep.sessionIV);
            Cipher<String, byte[]> aes = new StringCipherAdapter<>(new AESCipher(key, iv));
            plaintext = aes.decrypt(ep.payload);

            return plaintext;
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

        final static String B64 = "[a-zA-Z0-9/\\+]+=+";
        final static Pattern epPattern = Pattern.compile(String.format("(%1$s);(%1$s);(%1$s)", B64));
        public byte[] sessionKey;
        public byte[] sessionIV;
        public byte[] payload;

        public EncryptedPayload() {
        }

        public EncryptedPayload(String ep) {
            Matcher m = epPattern.matcher(ep);
            assert m.matches() : "Encrypted payload string malformed: " + epPattern.pattern();

            sessionKey = Base64Helper.decodeByte(m.group(1));
            sessionIV = Base64Helper.decodeByte(m.group(2));
            payload = Base64Helper.decodeByte(m.group(3));
        }

        @Override
        public String toString() {
            return String.format(
                    "%s;%s;%s",
                    Base64Helper.encodeByte(sessionKey),
                    Base64Helper.encodeByte(sessionIV),
                    Base64Helper.encodeByte(payload)
            );
        }
    }
}

