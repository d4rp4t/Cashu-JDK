package com.cashujdk.nut00;


import java.util.List;
import java.util.stream.Collectors;

import com.cashujdk.nut02.KeysetIdUtil;

//Inner token of v4 token. Has to belong to the same mint
public class InnerToken {
    public String keysetId;
    private List<Proof> proofs;

    public InnerToken(String keysetId, List<Proof> proofs) {
        this.keysetId = KeysetIdUtil.mapShortKeysetId(keysetId);
        this.proofs = proofs.stream().map(p -> {
            p.keysetId = this.keysetId;
            return p;
        })
        .collect(Collectors.toList());
    }

    public InnerToken() {
    }

    public List<Proof> getProofs(List<String> fullKeysetsIds) {
        if (this.proofs == null) return null;
        String fullKeysetId = KeysetIdUtil.mapLongKeysetId(this.keysetId, fullKeysetsIds);
        return this.proofs.stream().map(p -> {
            p.keysetId = fullKeysetId;
            return p;
        })
        .collect(Collectors.toList());
    }
    
    public List<Proof> getProofsShortId() {
        return this.proofs;
    }
}
