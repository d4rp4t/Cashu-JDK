package com.project.cryptography;

import com.project.nut00.BlindSignature;
import com.project.nut00.Proof;
import com.project.nut12.DLEQ;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Cashu {
    private static final byte[] DOMAIN_SEPARATOR = "Secp256k1_HashToCurve_Cashu_".getBytes(StandardCharsets.UTF_8);
    private static final SecP256K1Curve CURVE = new SecP256K1Curve();
    private static final ECPoint GENERATOR = ECC.DOMAIN.getG();
    private static final BigInteger CURVE_ORDER = CURVE.getOrder();



    /**
     * Converts a message string to a curve point using hash-to-curve
     */
    public static ECPoint messageToCurve(String message) {
        byte[] hash = message.getBytes(StandardCharsets.UTF_8);
        return hashToCurve(hash);
    }

    /**
     * Converts a hex string to a curve point using hash-to-curve
     */
    public static ECPoint hexToCurve(String hex) {
        byte[] bytes = Hex.decode(hex);
        return hashToCurve(bytes);
    }

    /**
     * Hash-to-curve implementation for secp256k1
     */
    public static ECPoint hashToCurve(byte[] x) {
        byte[] msgHash = sha256(concat(DOMAIN_SEPARATOR, x));

        for (long counter = 0; counter < Integer.MAX_VALUE; counter++) {
            byte[] counterBytes = longToBytes(counter);
            byte[] publicKeyBytes = concat(new byte[]{0x02}, sha256(concat(msgHash, counterBytes)));

            try {
                return CURVE.decodePoint(publicKeyBytes);
            } catch (Exception e) {
                // Continue to next counter if point is invalid
            }
        }
        throw new RuntimeException("Failed to find valid curve point");
    }

    /**
     * Converts a scalar (BigInteger) to a curve point by multiplying with generator
     */
    public static ECPoint scalarToPoint(BigInteger scalar) {
        return GENERATOR.multiply(scalar.mod(CURVE_ORDER));
    }

    /**
     * Computes B_ = Y + rG
     */
    public static ECPoint computeB_(ECPoint Y, BigInteger r) {
        ECPoint rG = GENERATOR.multiply(r.mod(CURVE_ORDER));
        return Y.add(rG);
    }

    /**
     * Computes C_ = kB_
     */
    public static ECPoint computeC_(ECPoint B_, BigInteger k) {
        return B_.multiply(k.mod(CURVE_ORDER));
    }

    /**
     * Computes DLEQ proof (e, s)
      */
    public static DLEQ computeProof(ECPoint B_, BigInteger a, BigInteger p) {
        //C_ - rK = kY + krG - krG = kY = C
        ECPoint C_ = computeC_(B_, a);
        ECPoint r1 = GENERATOR.multiply(p.mod(CURVE_ORDER));
        ECPoint r2 = B_.multiply(p.mod(CURVE_ORDER));
        ECPoint A = GENERATOR.multiply(a.mod(CURVE_ORDER));

        BigInteger e = computeE(r1, r2, A, C_);
        BigInteger s = p.add(a.multiply(e)).mod(CURVE_ORDER);

        return new DLEQ(e, s);
    }

    /**
     * Computes challenge e for DLEQ proof
     */
    public static BigInteger computeE(ECPoint R1, ECPoint R2, ECPoint K, ECPoint C_) {
        String concatenated = pointToHex(R1, false) + pointToHex(R2, false) +
                pointToHex(K, false) + pointToHex(C_, false);
        byte[] eBytes = concatenated.getBytes(StandardCharsets.UTF_8);
        byte[] hash = sha256(eBytes);
        return new BigInteger(1, hash).mod(CURVE_ORDER);
    }

    /**
     *  Verifies a proof
     */
    public static boolean verify(Proof proof, ECPoint A) {
        ECPoint Y = proof.secret.toCurve();
        return verifyProof(Y, proof.dleq.r, proof.c, proof.dleq.e, proof.dleq.s, A);
    }

    /**
     *   Verifies a blind signature
     */
    public static boolean verify(BlindSignature blindSig, ECPoint A, ECPoint B_) {
        return verifyProof(B_, blindSig.getC_(), blindSig.dleq.e, blindSig.dleq.s, A);
    }

    /**
     * Verifies DLEQ proof for blinded signature
     */
    public static boolean verifyProof(ECPoint B_, ECPoint C_, BigInteger e, BigInteger s, ECPoint A) {
        ECPoint sG = GENERATOR.multiply(s.mod(CURVE_ORDER));
        ECPoint eA = A.multiply(e.mod(CURVE_ORDER));
        ECPoint r1 = sG.subtract(eA);

        ECPoint sB_ = B_.multiply(s.mod(CURVE_ORDER));
        ECPoint eC_ = C_.multiply(e.mod(CURVE_ORDER));
        ECPoint r2 = sB_.subtract(eC_);

        BigInteger e_ = computeE(r1, r2, A, C_);
        return e.equals(e_);
    }

    /**
     * Verifies DLEQ proof for unblinded signature
     */
    public static boolean verifyProof(ECPoint Y, BigInteger r, ECPoint C, BigInteger e, BigInteger s, ECPoint A) {
        ECPoint rA = A.multiply(r.mod(CURVE_ORDER));
        ECPoint C_ = C.add(rA);
        ECPoint rG = GENERATOR.multiply(r.mod(CURVE_ORDER));
        ECPoint B_ = Y.add(rG);

        return verifyProof(B_, C_, e, s, A);
    }

    /**
     * Computes C = C_ - rA (unblinding)
     */
    public static ECPoint computeC(ECPoint C_, BigInteger r, ECPoint A) {
        ECPoint rA = A.multiply(r.mod(CURVE_ORDER));
        return C_.subtract(rA);
    }

    /**
     * Generates a random scalar for use as private key
     */
    public static BigInteger generateRandomScalar() {
        SecureRandom random = new SecureRandom();
        BigInteger scalar;
        do {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            scalar = new BigInteger(1, bytes);
        } while (scalar.equals(BigInteger.ZERO) || scalar.compareTo(CURVE_ORDER) >= 0);
        return scalar;
    }

    // Utility methods

    private static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static byte[] concat(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    private static byte[] longToBytes(long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

    public static String pointToHex(ECPoint point, boolean compressed) {
        byte[] bytes = pointToBytes(point, compressed);
        return Hex.toHexString(bytes).toLowerCase();
    }

    public static byte[] pointToBytes(ECPoint point, boolean compressed) {
        return point.getEncoded(compressed);
    }

    public static String scalarToHex(BigInteger scalar) {
        byte[] bytes = scalar.toByteArray();
        // Ensure we have exactly 32 bytes, padding with zeros if necessary
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 32 - bytes.length, bytes.length);
            bytes = padded;
        } else if (bytes.length > 32) {
            // Remove leading zero byte if present
            bytes = Arrays.copyOfRange(bytes, bytes.length - 32, bytes.length);
        }
        return Hex.toHexString(bytes).toLowerCase();
    }

    public static ECPoint hexToPoint(String hex) {
        byte[] bytes = Hex.decode(hex);
        return CURVE.decodePoint(bytes);
    }

    public static BigInteger hexToScalar(String hex) {
        byte[] bytes = Hex.decode(hex);
        return new BigInteger(1, bytes);
    }
}