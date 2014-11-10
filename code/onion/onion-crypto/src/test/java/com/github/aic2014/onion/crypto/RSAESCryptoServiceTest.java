package com.github.aic2014.onion.crypto;

import org.junit.Test;
import static org.junit.Assert.*;

import java.security.GeneralSecurityException;

public class RSAESCryptoServiceTest {
    public static String vulcan = "Eyjafjallaj\u00f6kull";

    @Test
    public void BasicTest() throws GeneralSecurityException {
        RSAESCryptoService service = new RSAESCryptoService();
        String e = service.encrypt(vulcan, service.getPublicKey());
        System.out.printf("rsa_aes(%s): %s%n", vulcan, e);

        String d = service.decrypt(e);
        assertEquals(vulcan, d);
    }
}
