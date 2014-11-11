package com.github.aic2014.onion.crypto.cipher;

import com.github.aic2014.onion.crypto.BouncyCastleBase;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * RSA cipher for asymmetric encryption of small amounts of data (typ. < 64 bytes).
 */
public class RSACipher extends BouncyCastleBase implements Cipher<byte[], byte[]> {

    private static String algo = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private javax.crypto.Cipher cipher;

    public RSACipher(KeyPair keyPair) throws GeneralSecurityException {
        this(keyPair.getPublic());
        this.privateKey = keyPair.getPrivate();
    }

    public RSACipher(PublicKey publicKey) throws GeneralSecurityException {
        this.publicKey = publicKey;
        cipher = javax.crypto.Cipher.getInstance(algo, getProviderName());
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    @Override
    public byte[] encrypt(byte[] plain) throws GeneralSecurityException {
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plain);
    }

    @Override
    public byte[] decrypt(byte[] encrypted) throws GeneralSecurityException {
        if (privateKey == null)
            throw new GeneralSecurityException("PrivateKey not set");

        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);
        return  cipher.doFinal(encrypted);
    }
}
