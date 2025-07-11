package com.cashujdk.nut23;

import com.cashujdk.nut00.BlindedMessage;
import com.cashujdk.nut00.Proof;
import com.cashujdk.nut05.PostMeltRequest;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

public class PostMeltBolt11Request extends PostMeltRequest {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<BlindedMessage> outputs;

    PostMeltBolt11Request(
        String quote,
        List<Proof> inputs,
        List<BlindedMessage> outputs
    ) {
        this.quote = quote;
        this.inputs = inputs;
        this.outputs = outputs;
    }
}
