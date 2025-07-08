package com.project.nut00;

public class CashuProtocolException extends RuntimeException {
    public CashuProtocolException(CashuProtocolError error) {
        super(error.detail);
    }
    public CashuProtocolError error;
}
