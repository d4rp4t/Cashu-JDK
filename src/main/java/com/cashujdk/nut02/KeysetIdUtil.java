package com.cashujdk.nut02;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.Optional;

public class KeysetIdUtil {

    public static String getId(byte version, Map<BigInteger, String> keys, String unit, Optional<Long> finalExpiry) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            // 1) sort public keys by amount ascending
            List<Map.Entry<BigInteger, String>> entries = new ArrayList<>(keys.entrySet());
            entries.sort(Comparator.comparing(Map.Entry::getKey));

            // 2) concatenate all public keys (hex -> bytes)
            HexFormat hex = HexFormat.of();
            int totalLen = entries.stream()
                    .mapToInt(e -> hex.parseHex(e.getValue()).length)
                    .sum();
            ByteBuffer concat = ByteBuffer.allocate(totalLen);
            for (Map.Entry<BigInteger, String> e : entries) {
                concat.put(hex.parseHex(e.getValue()));
            }
            byte[] keysBytes = concat.array();

            if (version == (byte) 0x01) {
                // include unit and optional final expiry
                byte[] unitBytes = ("unit:" + (unit == null ? "" : unit.toLowerCase()))
                        .getBytes(StandardCharsets.UTF_8);
                byte[] expiryBytes = finalExpiry
                        .map(v -> ("final_expiry:" + Long.toString(v)).getBytes(StandardCharsets.UTF_8))
                        .orElse(new byte[0]);

                byte[] toHash = new byte[keysBytes.length + unitBytes.length + expiryBytes.length];
                System.arraycopy(keysBytes, 0, toHash, 0, keysBytes.length);
                System.arraycopy(unitBytes, 0, toHash, keysBytes.length, unitBytes.length);
                if (expiryBytes.length > 0) {
                    System.arraycopy(expiryBytes, 0, toHash, keysBytes.length + unitBytes.length, expiryBytes.length);
                }

                byte[] hash = sha256.digest(toHash);

                byte[] out = new byte[1 + hash.length];
                out[0] = version;
                System.arraycopy(hash, 0, out, 1, hash.length);
                return hex.formatHex(out).toLowerCase(Locale.ROOT);
            } else if (version == (byte) 0x00) {
                // old version: hash only keys, take first 7 bytes, prefix 0x00
                byte[] hash = sha256.digest(keysBytes);
                byte[] shortHash = Arrays.copyOf(hash, 7);
                byte[] out = new byte[1 + shortHash.length];
                out[0] = version;
                System.arraycopy(shortHash, 0, out, 1, shortHash.length);
                return hex.formatHex(out).toLowerCase(Locale.ROOT);
            } else {
                throw new RuntimeException("Unrecognized keyset version: " + version);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    public static String mapLongKeysetId(String shortId, List<String> fullKeysetsIds) {
        if (shortId == null) {
            throw new RuntimeException("shortId is null");
        }
        if (shortId.startsWith("00")) {
            return shortId;
        }
        if (shortId.startsWith("01")) {
            for (String full : fullKeysetsIds) {
                if (full != null && full.startsWith(shortId)) {
                    return full;
                }
            }
            throw new RuntimeException("No matching full keyset id found for shortId: " + shortId);
        }
        throw new RuntimeException("Unsupported keyset id prefix for shortId: " + shortId);
    }


    public static String mapShortKeysetId(String fullId) {
        if (fullId == null) {
            throw new RuntimeException("fullId is null");
        }
        if (fullId.startsWith("00")) {
            return fullId;
        }
        if (fullId.startsWith("01")) {
            // Return first 8 bytes (16 hex chars) or first 8 characters if not hex;
            // assuming ID is hex-encoded, so 8 bytes = 16 hex chars.
            int lengthToKeep = 16;
            if (fullId.length() <= lengthToKeep) {
                return fullId;
            }
            return fullId.substring(0, lengthToKeep);
        }
        throw new RuntimeException("Unsupported keyset id prefix for fullId: " + fullId);
    }


}
