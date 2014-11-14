package com.github.aic2014.onion.crypto;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;
import java.security.*;

public class GeneratorTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void generateRSAKeyPairTest() throws GeneralSecurityException {
        KeyPair kp = Generators.generateRSAKeyPair();
        PrivateKey pem = kp.getPrivate();
        PublicKey pub = kp.getPublic();
        assertEquals("RSA", pem.getAlgorithm());
        assertEquals("RSA", pub.getAlgorithm());
        printKey("Gen. RSA private key", pem.getEncoded());
        printKey("Gen. RSA public key", pub.getEncoded());
    }

    @Test
    public void generateAESKeyTest() throws GeneralSecurityException {
        SecretKey k = Generators.generateAESKey();
        assertEquals("AES", k.getAlgorithm());
        printKey("Gen. AES key", k.getEncoded());
    }

    @Test
    public void generateIV() {
        IvParameterSpec iv = Generators.generateIV();
        printKey("Gen. IV", iv.getIV());
    }

    private void printKey(String name, byte[] key)
    {
        logger.info(String.format(
                "%s (%d bytes): %.64s[..]",
                name,
                key.length,
                DatatypeConverter.printHexBinary(key)
        ));
    }
}
