package com.github.aic2014.onion.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;

public class Generators extends BouncyCastleBase {
    /**
     * Generates an RSA key pair with a default bit size.
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateRSAKeyPair() throws GeneralSecurityException {
        return generateRSAKeyPair(1024);
    }

    public static KeyPair generateRSAKeyPair(int b) throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", getProviderName());
        generator.initialize(b);
        return generator.generateKeyPair();
    }

    /**
     * Generates an AES key with a default bit size.
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateAESKey() throws GeneralSecurityException {
        return generateAESKey(128);
    }

    /**
     * Generates an AES key.
     * @param b Key size in bits. > 128 needs Oracle Unlimited JCE Jurisdiction Policy installed.
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateAESKey(int b) throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance("AES", getProviderName());
        generator.init(b);
        return generator.generateKey();
    }

    /**
     * Generates a random IV for AES block cipher modes.
     */
    public static IvParameterSpec generateIV() {
        byte iv[] = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
