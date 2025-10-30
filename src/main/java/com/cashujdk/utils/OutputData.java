package com.cashujdk.utils;

import com.cashujdk.nut00.BlindedMessage;
import com.cashujdk.nut00.ISecret;

public class OutputData {
    public BlindedMessage blindedMessage;
    public ISecret secret;
    public byte[] blindingFactor;
}
