package com.cashujdk.nut14;

import com.cashujdk.nut11.P2PkBuilder;
import com.cashujdk.nut11.P2PKProofSecret;
import com.cashujdk.cryptography.Cashu;
import org.bouncycastle.math.ec.ECPoint;
import java.util.List;
import java.util.stream.Collectors;

public class HTLCBuilder extends P2PkBuilder {
    private ECPoint hashLock;

    public ECPoint getHashLock() {
        return hashLock;
    }

    public void setHashLock(ECPoint hashLock) {
        this.hashLock = hashLock;
    }

    public static HTLCBuilder load(HTLCProofSecret proofSecret) {
        ECPoint hashLock = Cashu.hexToPoint(proofSecret.getData());
        P2PkBuilder innerBuilder = P2PkBuilder.load(new P2PKProofSecret(proofSecret.getNonce(), proofSecret.getData(), proofSecret.getTags()));
        List<ECPoint> filteredPubkeys = innerBuilder.getPubkeys().stream()
                .filter(pk -> !pk.equals(hashLock))
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
        P2PkBuilder innerBuilder = new P2PkBuilder();
        innerBuilder.setLockTime(getLockTime());
        innerBuilder.setPubkeys(getPubkeys());
        innerBuilder.setRefundPubkeys(getRefundPubkeys());
        innerBuilder.setSignatureThreshold(getSignatureThreshold());
        innerBuilder.setSigFlag(getSigFlag());
        innerBuilder.setNonce(getNonce());
        List<ECPoint> pubkeysWithHashLock = innerBuilder.getPubkeys();
        pubkeysWithHashLock.add(0, hashLock);
        innerBuilder.setPubkeys(pubkeysWithHashLock);
        P2PKProofSecret p2pkProof = innerBuilder.build();

        return new HTLCProofSecret(p2pkProof.getNonce(), Cashu.pointToHex(hashLock, true), p2pkProof.getTags());
    }
}
