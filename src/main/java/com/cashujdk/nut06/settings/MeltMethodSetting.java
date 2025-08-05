package com.cashujdk.nut06.settings;

import java.math.BigInteger;
import java.util.Optional;

public class MeltMethodSetting {

    public String method;
    public String unit;
    public Optional<Long> min_amount;
    // I'm sorry, but I had to use BigInteger here
    public Optional<BigInteger> max_amount;
    public Optional<Object> options;


    public MeltMethodSetting() {}
}
