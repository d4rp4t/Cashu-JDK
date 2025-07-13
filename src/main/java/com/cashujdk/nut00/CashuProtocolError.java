package com.cashujdk.nut00;

public class CashuProtocolError {
    public int code;
    public String detail;

    public CashuProtocolError(int code, String detail) {
        this.code = code;
        this.detail = detail;
    }
}
