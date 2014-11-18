package com.github.aic2014.onion.crypto;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Internal class to ease RSA public key conversion to and from String.
 */
class PublicKeyString {

    private PublicKey publicKey;

    public PublicKeyString(PublicKey rsaPublicKey) {
        this.publicKey = rsaPublicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PublicKeyString(String publicKey) throws GeneralSecurityException {
        if (publicKey == null || publicKey.isEmpty())
            throw new GeneralSecurityException("publicKey must not be empty");

        KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
        byte[] encoded = Base64Helper.decodeByte(publicKey);
        this.publicKey = rsaKeyFactory.generatePublic(new X509EncodedKeySpec(encoded));
    }

    @Override
    public String toString() {
        return Base64Helper.encodeByte(publicKey.getEncoded());
    }
}
