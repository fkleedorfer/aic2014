package com.github.aic2014.onion.crypto;

import com.github.aic2014.onion.crypto.cipher.*;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static org.junit.Assert.*;

import java.security.GeneralSecurityException;
import java.security.KeyPair;

public class CipherTest {
    public static String vulcan = "Eyjafjallaj\u00f6kull";

    @Test
    public void RSACryptorTest() throws GeneralSecurityException {
        KeyPair kp = Generators.generateRSAKeyPair();
        Cipher<String, String> rsa = new StringCipherAdapter<>(new Base64CipherAdapter<>(new RSACipher(kp)));
        String e = rsa.encrypt(vulcan);
        System.out.printf("rsa(%s): %s%n", vulcan, e);
        String d = rsa.decrypt(e);
        assertEquals(vulcan, d);
    }

    @Test
    public void AESCryptorTest() throws GeneralSecurityException {
        SecretKey key = Generators.generateAESKey();
        IvParameterSpec iv = Generators.generateIV();
        Cipher<String, String> aes = new StringCipherAdapter<>(new Base64CipherAdapter<>(new AESCipher(key, iv)));
        String e = aes.encrypt(vulcan);
        System.out.printf("aes(%s): %s%n", vulcan, e);
        String d = aes.decrypt(e);
        assertEquals(vulcan, d);
    }
}
