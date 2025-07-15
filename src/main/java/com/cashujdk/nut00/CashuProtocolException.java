package com.cashujdk.nut00;

public class CashuProtocolException extends RuntimeException {
    public CashuProtocolException(CashuProtocolError error) {
        super(error.detail);
    }
    public CashuProtocolException() {}
    public CashuProtocolError error;
}
