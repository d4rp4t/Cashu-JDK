package com.cashujdk.nut23;

import com.cashujdk.nut00.BlindSignature;
import com.cashujdk.nut00.BlindedMessage;
import com.cashujdk.nut05.PostMeltQuoteRequest;
import com.cashujdk.nut05.PostMeltQuoteResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.List;

public class PostMeltQuoteBolt11Response extends PostMeltQuoteResponse {

    public String request;

    @JsonProperty("fee_reserve")
    public BigInteger feeReserve;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("payment_preimage")
    public String paymentPreimage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<BlindSignature> change;

    PostMeltQuoteBolt11Response(
            String request,
            String quote,
            BigInteger amount,
            String unit,
            BigInteger feeReserve,
            String state,
            BigInteger expiry,
            String paymentPreimage,
            List<BlindSignature> change
    ){
        this.request = request;
        this.quote = quote;
        this.amount = amount;
        this.unit = unit;
        this.feeReserve = feeReserve;
        this.state = state;
        this.expiry = expiry;
        this.paymentPreimage = paymentPreimage;
        this.change = change;
    }
}
