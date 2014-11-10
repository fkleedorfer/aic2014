package com.github.aic2014.onion.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Generators {
    /**
     * Generates an RSA key pair with a default bit size.
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        return generateRSAKeyPair(1024);
    }

    public static KeyPair generateRSAKeyPair(int b) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(b);
        return generator.generateKeyPair();
    }

    /**
     * Generates an AES key with a default bit size.
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        return generateAESKey(128);
    }

    /**
     * Generates an AES key.
     * @param b Key size in bits. > 128 needs Oracle Unlimited JCE Jurisdiction Policy installed.
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateAESKey(int b) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
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
