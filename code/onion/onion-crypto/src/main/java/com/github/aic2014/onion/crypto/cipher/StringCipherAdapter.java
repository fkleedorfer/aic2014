package com.github.aic2014.onion.crypto.cipher;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Adapter to encode the plain text byte arrays as UTF-8 strings.
 * @param <E> Encrypted data object type
 */
public class StringCipherAdapter<E> implements Cipher<String, E> {

    private Cipher<byte[], E> inner;

    public StringCipherAdapter(Cipher<byte[], E> inner)
    {
        this.inner = inner;
    }

    @Override
    public E encrypt(String plain) throws GeneralSecurityException {
        byte[] p = plain.getBytes(StandardCharsets.UTF_8);
        return inner.encrypt(p);
    }

    @Override
    public String decrypt(E encrypted) throws GeneralSecurityException {
        byte[] e = inner.decrypt(encrypted);
        return new String(e, StandardCharsets.UTF_8);
    }
}
