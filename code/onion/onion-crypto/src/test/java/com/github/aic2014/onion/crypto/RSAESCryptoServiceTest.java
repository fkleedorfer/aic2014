package com.github.aic2014.onion.crypto;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import java.security.GeneralSecurityException;

public class RSAESCryptoServiceTest {
    public static String vulcan = "Eyjafjallaj\u00f6kull";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void BasicTest() throws GeneralSecurityException {
        RSAESCryptoService service = new RSAESCryptoService();
        String e = service.encrypt(vulcan, service.getPublicKey());
        logger.info("rsa_aes({}): {}", vulcan, e);

        String d = service.decrypt(e);
        assertEquals(vulcan, d);
    }
}
