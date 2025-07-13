package com.cashujdk.nut01;

import java.math.BigInteger;
import java.util.Map;

public class KeysetItemResponse {
    public String id;
    public String unit;
    public Map<BigInteger, String> keys;

    public KeysetItemResponse(String id, String unit, Map<BigInteger, String> keys) {
        this.id = id;
        this.unit = unit;
        this.keys = keys;
    }
}
