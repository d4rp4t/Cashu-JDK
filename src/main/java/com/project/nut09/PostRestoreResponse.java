package com.project.nut09;

import com.project.nut00.BlindSignature;
import com.project.nut00.BlindedMessage;

import java.util.List;

public class PostRestoreResponse {
    public List<BlindedMessage> outputs;
    public List<BlindSignature> signatures;
}
