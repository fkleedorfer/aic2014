package com.github.aic2014.onion.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * Abstract base class for dependency on the BouncyCastle Security provider.
 */
public abstract class BouncyCastleBase {
    static {
        if (Security.getProvider(getProviderName()) == null)
            Security.addProvider(new BouncyCastleProvider());
    }

    protected static String getProviderName()
    {
        return "BC";
    }
}
