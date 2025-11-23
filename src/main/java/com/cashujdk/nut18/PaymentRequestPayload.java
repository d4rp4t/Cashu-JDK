package com.cashujdk.nut18;

import com.cashujdk.nut00.Proof;

import java.util.Optional;

public class PaymentRequestPayload {
    public PaymentRequestPayload() {}
    public String id;
    public Optional<String> memo;
    public String mint;
    public String unit;
    public Proof[] proofs;
}
