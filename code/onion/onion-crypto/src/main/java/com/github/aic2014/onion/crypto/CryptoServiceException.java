package com.github.aic2014.onion.crypto;

public class CryptoServiceException extends RuntimeException {
    public CryptoServiceException(String msg)
    {
        super(msg);
    }

    public CryptoServiceException(Throwable inner)
    {
        super(inner.getMessage(), inner);
    }

    public CryptoServiceException(String msg, Throwable inner)
    {
        super(msg, inner);
    }
}
