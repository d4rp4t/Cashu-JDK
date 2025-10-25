package com.cashujdk.nut13;

import com.cashujdk.nut00.StringSecret;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicSeed;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import java.util.HexFormat;

public class Nut13 {
    public static final String PURPOSE = "129372'";
    public static final String DOMAIN_SEPARATOR = "Cashu_KDF_HMAC_SHA256";

    /**
     * @param keyBytes    secret key bytes (required)
     * @param messageBytes message bytes (may be empty or null treated as zero-length)
     * @return 32-bytes string (preserves leading zeros)
     */
    public static byte[] hmacSha256(byte[] keyBytes, byte[] messageBytes) {
        if (keyBytes == null) throw new IllegalArgumentException("keyBytes must not be null");
        if (messageBytes == null) messageBytes = new byte[0];

        // Create HMac(SHA-256)
        Mac hmac = new HMac(new SHA256Digest());

        // Initialize with raw key bytes
        hmac.init(new KeyParameter(keyBytes));

        // Process message bytes
        hmac.update(messageBytes, 0, messageBytes.length);

        // Get output (32 bytes)
        byte[] out = new byte[hmac.getMacSize()];
        hmac.doFinal(out, 0);

        // Convert to 64-char hex using java.util.HexFormat
        return out;
    }

    /**
     * Creates NUT-13 derivation path
     */
    public static List<ChildNumber> getNut13DerivationPath(String keysetId, int counter, boolean secretOrr) {
        int keysetIdInt = getKeysetIdInt(keysetId);

        return Arrays.asList(
                new ChildNumber(129372, true),  // purpose
                new ChildNumber(0, true),       // coin_type
                new ChildNumber(keysetIdInt, true),  // keyset_id
                new ChildNumber(counter, true), // counter
                new ChildNumber(secretOrr ? 0 : 1, false)  // secret_or_blinding
        );
    }

    /**
     *  Convert keysetId to integer
     */
    public static int getKeysetIdInt(String keysetId) {
        // 4 - take the first 14 characters of the hex-encoded hash
        //5 - prefix it with a keyset ID version byte
        if(keysetId.length() != 16 && keysetId.length() != 8) {
            throw new IllegalArgumentException("Invalid keysetId length: " + keysetId.length());
        }
        // 2**31-1 is max value of int
        // since we're doing mod by Integer.MAX_VALUE, max amount of it will be Integer.MAX_VALUE -1
        return new BigInteger(keysetId, 16).mod(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
    }

    /**
     * Derives blideing factoir from mnemonic
     */
    public static byte[] deriveBlindingFactor(List<String> mnemonic, String keysetId, int counter)
            throws Exception
    {
        byte[] seed = deriveSeed(mnemonic);
        return deriveBlindingFactor(seed, keysetId, counter);
    }

    /**
     * Derives secret from mnemonic
     */
    public static StringSecret deriveSecret(List<String> mnemonic, String keysetId, int counter)
            throws Exception {
        byte[] seed = deriveSeed(mnemonic);
        return deriveSecret(seed, keysetId, counter);
    }

    /**
     * Derives blinding factor from seed
     */
    public static byte[] deriveBlindingFactor(byte[] seed, String keysetId, int counter) {
        if (keysetId.substring(0, 2).contentEquals("01")) {
            return deriveBlindingFactorV2(seed, keysetId, counter);
        } else if (keysetId.substring(0, 2).contentEquals("00")) {
            return deriveBlindingFactorV1(seed, keysetId, counter);
        } else {
            throw new RuntimeException("Unrecognized keyset ID for blinding factor derivation");
        }
    }

    public static byte[] deriveBlindingFactorV1(byte[] seed, String keysetId, int counter) {
        List<ChildNumber> path = getNut13DerivationPath(keysetId, counter, false);
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);

        DeterministicKey derivedKey = path.stream()
                .reduce(masterKey,
                        HDKeyDerivation::deriveChildKey,
                        (a, b) -> b);

        return derivedKey.getPrivKeyBytes();
    }

    public static byte[] deriveBlindingFactorV2(byte[] seed, String keysetId, int counter) {
        if (seed == null) throw new IllegalArgumentException("seed must not be null");
        // Decode hex using java.util.HexFormat
        byte[] keysetIdBytes = (keysetId == null || keysetId.isEmpty())
                ? new byte[0]
                : HexFormat.of().parseHex(keysetId);

        // Build message: b"Cashu_KDF_HMAC_SHA256" || keyset_id_bytes || counter_k_bytes || derivation_type_byte
        byte[] purpose = "Cashu_KDF_HMAC_SHA256".getBytes(StandardCharsets.UTF_8);

        // counter encoded as unsigned 64-bit big-endian
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(((long) counter) & 0xffffffffffffffffL);
        byte[] counterBytes = bb.array();

        byte derivationType = (byte) 0x01; // blinded messages

        // concatenate
        int totalLen = purpose.length + keysetIdBytes.length + counterBytes.length + 1;
        byte[] message = new byte[totalLen];
        int pos = 0;
        System.arraycopy(purpose, 0, message, pos, purpose.length); pos += purpose.length;
        System.arraycopy(keysetIdBytes, 0, message, pos, keysetIdBytes.length); pos += keysetIdBytes.length;
        System.arraycopy(counterBytes, 0, message, pos, counterBytes.length); pos += counterBytes.length;
        message[pos] = derivationType;

        // HMAC-SHA256(seed, message)
        return hmacSha256(seed, message);
    }

    /**
     * Derives secret from seed
     */
    public static StringSecret deriveSecret(byte[] seed, String keysetId, int counter) {
        if (keysetId.substring(0, 2).contentEquals("01")) {
            return deriveSecretV2(seed, keysetId, counter);
        } else if (keysetId.substring(0, 2).contentEquals("00")) {
            return deriveSecretV1(seed, keysetId, counter);
        } else {
            throw new RuntimeException("Unrecognized keyset ID for blinding factor derivation");
        }
    }

    public static StringSecret deriveSecretV1(byte[] seed, String keysetId, int counter) {
        List<ChildNumber> path = getNut13DerivationPath(keysetId, counter, true);
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicKey derivedKey = path.stream()
                .reduce(masterKey,
                        HDKeyDerivation::deriveChildKey,
                        (a, b) -> b);

        byte[] privateKeyBytes = derivedKey.getPrivKeyBytes();
        String hexString = bytesToHex(privateKeyBytes).toLowerCase();

        return new StringSecret(hexString);
    }

    public static StringSecret deriveSecretV2(byte[] seed, String keysetId, int counter) {
        if (seed == null) throw new IllegalArgumentException("seed must not be null");
        // Decode hex using java.util.HexFormat
        byte[] keysetIdBytes = (keysetId == null || keysetId.isEmpty())
                ? new byte[0]
                : HexFormat.of().parseHex(keysetId);

        // Build message: b"Cashu_KDF_HMAC_SHA256" || keyset_id_bytes || counter_k_bytes || derivation_type_byte
        byte[] purpose = "Cashu_KDF_HMAC_SHA256".getBytes(StandardCharsets.UTF_8);

        // counter encoded as unsigned 64-bit big-endian
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(((long) counter) & 0xffffffffffffffffL);
        byte[] counterBytes = bb.array();

        byte derivationType = (byte) 0x00; // secrets

        // concatenate
        int totalLen = purpose.length + keysetIdBytes.length + counterBytes.length + 1;
        byte[] message = new byte[totalLen];
        int pos = 0;
        System.arraycopy(purpose, 0, message, pos, purpose.length); pos += purpose.length;
        System.arraycopy(keysetIdBytes, 0, message, pos, keysetIdBytes.length); pos += keysetIdBytes.length;
        System.arraycopy(counterBytes, 0, message, pos, counterBytes.length); pos += counterBytes.length;
        message[pos] = derivationType;

        // HMAC-SHA256(seed, message)
        return new StringSecret(bytesToHex(hmacSha256(seed, message)).toLowerCase());
    }

    /**
     * Converts mnemonic to seed
     */
    private static byte[] deriveSeed(List<String> mnemonic) throws Exception {
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", 0);
        return seed.getSeedBytes();
    }

    /**
     * Converts bytes to hex
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Generates new mnemonic
     */
    public static List<String> generateMnemonic() throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] entropy = new byte[16]; // 128 bits = 12 words
        random.nextBytes(entropy);

        return MnemonicCode.INSTANCE.toMnemonic(entropy);
    }

}
