package com.cashujdk.nut03;

import java.util.List;

import com.cashujdk.nut00.BlindedMessage;
import com.cashujdk.nut00.Proof;

public class PostSwapRequest {
    public List<Proof> inputs;
    public List<BlindedMessage> outputs;
}
