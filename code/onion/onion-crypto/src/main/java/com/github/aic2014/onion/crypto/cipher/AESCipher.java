package com.github.aic2014.onion.crypto.cipher;

import com.github.aic2014.onion.crypto.Generators;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;

public class AESCipher extends BouncyCastleCipher<byte[], byte[]> {

    private static String algo = "AES/CTR/PKCS5Padding";

    private javax.crypto.Cipher cipher;
    private SecretKey key;
    private IvParameterSpec ivSpec;

    public AESCipher(SecretKey key, IvParameterSpec iv) throws GeneralSecurityException {
        this.key = key;
        this.ivSpec = iv;
        cipher = javax.crypto.Cipher.getInstance(algo);
    }

    public SecretKey getKey()
    {
        return key;
    }

    public IvParameterSpec getIvSpec()
    {
        return ivSpec;
    }

    @Override
    public byte[] encrypt(byte[] plain) throws GeneralSecurityException {
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, ivSpec);
        return cipher.doFinal(plain);
    }

    @Override
    public byte[] decrypt(byte[] encrypted) throws GeneralSecurityException {
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(encrypted);
    }
}
