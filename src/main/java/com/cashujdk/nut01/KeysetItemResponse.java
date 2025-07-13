package com.cashujdk.nut01;

import java.math.BigInteger;
import java.util.Dictionary;

public class KeysetItemResponse {
    public String id;
    public String unit;
    public Dictionary<BigInteger, String> keys;

    public KeysetItemResponse(String id, String unit, Dictionary<BigInteger, String> keys) {
        this.id = id;
        this.unit = unit;
        this.keys = keys;
    }
}
