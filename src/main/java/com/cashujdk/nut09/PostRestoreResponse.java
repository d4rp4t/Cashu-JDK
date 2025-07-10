package com.cashujdk.nut09;

import java.util.List;

import com.cashujdk.nut00.BlindSignature;
import com.cashujdk.nut00.BlindedMessage;

public class PostRestoreResponse {
    public List<BlindedMessage> outputs;
    public List<BlindSignature> signatures;
}
