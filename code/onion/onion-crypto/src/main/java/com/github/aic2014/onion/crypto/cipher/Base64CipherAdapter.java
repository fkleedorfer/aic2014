package com.github.aic2014.onion.crypto.cipher;

import com.github.aic2014.onion.crypto.Base64Helper;

import java.security.GeneralSecurityException;

/**
 * Adapter to encode the encrypted byte arrays as Base64.
 * @param <P> Plain data object type
 */
public class Base64CipherAdapter<P> implements Cipher<P, String> {

    private Cipher<P, byte[]> inner;

    public Base64CipherAdapter(Cipher<P, byte[]> inner)
    {
        this.inner = inner;
    }

    @Override
    public String encrypt(P plain) throws GeneralSecurityException {
        byte[] e = inner.encrypt(plain);
        return Base64Helper.encodeByte(e);
    }

    @Override
    public P decrypt(String encrypted) throws GeneralSecurityException {
        byte[] e = Base64Helper.decodeByte(encrypted);
        return inner.decrypt(e);
    }
}
