package com.cashujdk.nut11;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import com.cashujdk.cryptography.Cashu;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class P2PkBuilder {
    private Instant lockTime;
    private List<ECPoint> refundPubkeys;
    private int signatureThreshold = 1;
    private List<ECPoint> pubkeys;
    private String sigFlag;
    private String nonce;

    public Instant getLockTime() { return lockTime; }
    public void setLockTime(Instant lockTime) { this.lockTime = lockTime; }

    public List<ECPoint> getRefundPubkeys() { return refundPubkeys; }
    public void setRefundPubkeys(List<ECPoint> refundPubkeys) { this.refundPubkeys = refundPubkeys; }

    public int getSignatureThreshold() { return signatureThreshold; }
    public void setSignatureThreshold(int signatureThreshold) { this.signatureThreshold = signatureThreshold; }

    public List<ECPoint> getPubkeys() { return pubkeys; }
    public void setPubkeys(List<ECPoint> pubkeys) { this.pubkeys = pubkeys; }

    public String getSigFlag() { return sigFlag; }
    public void setSigFlag(String sigFlag) { this.sigFlag = sigFlag; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public P2PKProofSecret build() {

        if (pubkeys == null || pubkeys.isEmpty()) {
            throw new IllegalStateException("pubkeys must not be null or empty");
        }

        List<String[]> tags = new ArrayList<>();

        if (pubkeys.size() > 1) {
            String[] pubkeyTags = Stream.concat(
                    Stream.of("pubkeys"),
                    pubkeys.stream().skip(1).map(this::pointToHex)
            ).toArray(String[]::new);
            tags.add(pubkeyTags);
        }

        if (sigFlag != null && !sigFlag.isEmpty()) {
            tags.add(new String[]{"sigflag", sigFlag});
        }

        if (lockTime != null) {
            tags.add(new String[]{"locktime", String.valueOf(lockTime.getEpochSecond())});

            if (refundPubkeys != null && !refundPubkeys.isEmpty()) {
                String[] refundTags = Stream.concat(
                        Stream.of("refund"),
                        refundPubkeys.stream().map(this::pointToHex)
                ).toArray(String[]::new);
                tags.add(refundTags);
            }
        }

        if (signatureThreshold > 1 && pubkeys.size() >= signatureThreshold) {
            tags.add(new String[]{"n_sigs", String.valueOf(signatureThreshold)});
        }

        P2PKProofSecret proofSecret = new P2PKProofSecret();
        proofSecret.setData(pointToHex(pubkeys.get(0)));
        proofSecret.setNonce(nonce != null ? nonce : generateRandomHex(32));
        proofSecret.setTags(tags.toArray(new String[0][]));

        return proofSecret;
    }

    public static P2PkBuilder load(P2PKProofSecret proofSecret) {
        P2PkBuilder builder = new P2PkBuilder();
        ECPoint primaryPubkey = Cashu.hexToPoint(proofSecret.getData());

        Optional<String[]> pubkeysTag = findTag(proofSecret.getTags(), "pubkeys");
        if (pubkeysTag.isPresent() && pubkeysTag.get().length > 1) {
            List<ECPoint> allPubkeys = Stream.concat(
                    Stream.of(primaryPubkey),
                    Arrays.stream(pubkeysTag.get()).skip(1).map(P2PkBuilder::hexToPoint)
            ).collect(Collectors.toList());
            builder.setPubkeys(allPubkeys);
        } else {
            builder.setPubkeys(Collections.singletonList(primaryPubkey));
        }

        findTag(proofSecret.getTags(), "locktime")
                .filter(tag -> tag.length > 1)
                .ifPresent(tag -> {
                    try {
                        long epochSeconds = Long.parseLong(tag[1]);
                        builder.setLockTime(Instant.ofEpochSecond(epochSeconds));
                    } catch (NumberFormatException ignored) {}
                });

        findTag(proofSecret.getTags(), "refund")
                .filter(tag -> tag.length > 1)
                .ifPresent(tag -> {
                    List<ECPoint> refunds = Arrays.stream(tag)
                            .skip(1)
                            .map(P2PkBuilder::hexToPoint)
                            .collect(Collectors.toList());
                    builder.setRefundPubkeys(refunds);
                });

        findTag(proofSecret.getTags(), "sigflag")
                .filter(tag -> tag.length > 1)
                .ifPresent(tag -> builder.setSigFlag(tag[1]));

        findTag(proofSecret.getTags(), "n_sigs")
                .filter(tag -> tag.length > 1)
                .ifPresent(tag -> {
                    try {
                        builder.setSignatureThreshold(Integer.parseInt(tag[1]));
                    } catch (NumberFormatException ignored) {}
                });

        builder.setNonce(proofSecret.getNonce());
        return builder;
    }

    private static Optional<String[]> findTag(String[][] tags, String tagName) {
        if (tags == null) return Optional.empty();

        return Arrays.stream(tags)
                .filter(tag -> tag.length > 0 && tagName.equals(tag[0]))
                .findFirst();
    }

    private String pointToHex(ECPoint point) {
        //always compress
        return Cashu.pointToHex(point, true);
    }

    private static ECPoint hexToPoint(String hex) {
        return Cashu.hexToPoint(hex);
    }

    private String generateRandomHex(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Hex.toHexString(bytes).toLowerCase();
    }

}
