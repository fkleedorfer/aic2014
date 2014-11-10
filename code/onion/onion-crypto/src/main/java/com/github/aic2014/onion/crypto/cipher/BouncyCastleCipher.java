package com.github.aic2014.onion.crypto.cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * Cipher class which depends on the BouncyCastle Security provider.
 */
public abstract class BouncyCastleCipher<P, E> implements Cipher<P, E> {
    static {
        if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
    }
}
