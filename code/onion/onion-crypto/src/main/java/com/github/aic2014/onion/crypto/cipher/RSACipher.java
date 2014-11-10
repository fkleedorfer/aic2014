package com.github.aic2014.onion.crypto.cipher;

import com.github.aic2014.onion.crypto.Generators;

import java.security.*;

public class RSACipher extends BouncyCastleCipher<byte[], byte[]> {

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
        cipher = javax.crypto.Cipher.getInstance(algo);
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
