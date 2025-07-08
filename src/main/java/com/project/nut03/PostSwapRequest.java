package com.project.nut03;

import com.project.nut00.BlindedMessage;
import com.project.nut00.Proof;
import java.util.List;

public class PostSwapRequest {
    public List<Proof> inputs;
    public List<BlindedMessage> outputs;
}
