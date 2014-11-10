package com.github.aic2014.onion.crypto;

import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;

public class Base64Helper {

    public static String encodeString(String msg) {
        if (msg.isEmpty())
            return "";

        return encodeByte(msg.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeString(String msg) {
        if (msg.isEmpty())
            return "";

        return new String(decodeByte(msg), StandardCharsets.UTF_8);
    }

    public static String encodeByte(byte[] msg) {
        return new String(Base64.encode(msg));
    }

    public static byte[] decodeByte(String msg) {
        return Base64.decode(msg.getBytes());
    }
}
