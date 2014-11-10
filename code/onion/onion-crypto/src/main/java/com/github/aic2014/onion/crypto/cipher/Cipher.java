package com.github.aic2014.onion.crypto.cipher;

import java.security.GeneralSecurityException;

/**
 * Cipher interface which allows encryption and decryption of some object formats.
 * @param <P> Plain data object type
 * @param <E> Encrypted data object type
 */
public interface Cipher<P, E> {
    public E encrypt(P P) throws GeneralSecurityException;
    public P decrypt(E encrypted) throws GeneralSecurityException;
}
