package com.cashujdk.nut14;

import com.cashujdk.nut11.P2PkBuilder;
import com.cashujdk.nut11.P2PKProofSecret;
import com.cashujdk.cryptography.Cashu;
import org.bouncycastle.math.ec.ECPoint;
import java.util.List;
import java.util.stream.Collectors;

public class HTLCBuilder extends P2PkBuilder {
    private String hashLock;

    /*
     * ugly hack to reuse P2PkBuilder for HTLCs.
     * P2PkBuilder expects a pubkey in `data` field, but we need to store a hashlock instead
     *
     * we inject a dummy pubkey so the loader doesn’t break, then remove it after load/build.
     */
    private final static String dummy = "020000000000000000000000000000000000000000000000000000000000000001";

    public String getHashLock() {
        return hashLock;
    }

    public void setHashLock(String hashLock) {
        this.hashLock = hashLock;
    }

    public static HTLCBuilder load(HTLCProofSecret proofSecret) {
        String hashLock = proofSecret.getData();
        P2PKProofSecret tempProofSecret = new P2PKProofSecret(proofSecret.getNonce(), dummy, proofSecret.getTags());
        P2PkBuilder innerBuilder = P2PkBuilder.load(tempProofSecret);

        List<ECPoint> filteredPubkeys = innerBuilder.getPubkeys().stream()
                .filter(pk -> !pk.equals(Cashu.hexToPoint(dummy)))
                .collect(Collectors.toList());

        innerBuilder.setPubkeys(filteredPubkeys);
        HTLCBuilder builder = new HTLCBuilder();

        builder.setHashLock(hashLock);
        builder.setLockTime(innerBuilder.getLockTime());
        builder.setPubkeys(innerBuilder.getPubkeys());
        builder.setRefundPubkeys(innerBuilder.getRefundPubkeys());
        builder.setSignatureThreshold(innerBuilder.getSignatureThreshold());
        builder.setSigFlag(innerBuilder.getSigFlag());
        builder.setNonce(innerBuilder.getNonce());
        return builder;
    }

    public HTLCProofSecret build() {
        if(hashLock.length() != 64) {
            throw new IllegalArgumentException("hashLock length must be 64");
        }
        P2PkBuilder innerBuilder = new P2PkBuilder();
        innerBuilder.setLockTime(getLockTime());
        innerBuilder.setPubkeys(getPubkeys());
        innerBuilder.setRefundPubkeys(getRefundPubkeys());
        innerBuilder.setSignatureThreshold(getSignatureThreshold());
        innerBuilder.setSigFlag(getSigFlag());
        innerBuilder.setNonce(getNonce());

        List<ECPoint> pubkeysWithHashLock = new java.util.ArrayList<>();
        pubkeysWithHashLock.add(Cashu.hexToPoint(dummy));
        if (innerBuilder.getPubkeys() != null) {
            pubkeysWithHashLock.addAll(innerBuilder.getPubkeys());
        }
        innerBuilder.setPubkeys(pubkeysWithHashLock);
        P2PKProofSecret p2pkProof = innerBuilder.build();

        return new HTLCProofSecret(p2pkProof.getNonce(), hashLock, p2pkProof.getTags());
    }
}
