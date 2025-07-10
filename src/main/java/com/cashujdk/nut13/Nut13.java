package com.cashujdk.nut13;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicSeed;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class Nut13 {
    public static final String PURPOSE = "129372'";
    private static final long MOD = (long) Math.pow(2, 31) - 1;


    public static class StringSecret {
        private final String value;

        public StringSecret(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
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
        // 2**31-1 is max value of integer
        BigInteger keysetIdInt = new BigInteger(keysetId ,16).mod(BigInteger.valueOf(Integer.MAX_VALUE));
        // since we're doing mod by Integer.MAX_VALUE, max amount of it will be Integer.MAX_VALUE -1
        return keysetIdInt.intValue();
    }

    /**
     * Derives blideing factoir from mnemonic
     */
    public static byte[] deriveBlindingFactor(List<String> mnemonic, String keysetId, int counter)
            throws Exception {
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
        List<ChildNumber> path = getNut13DerivationPath(keysetId, counter, false);
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);

        DeterministicKey derivedKey = path.stream()
                .reduce(masterKey,
                        HDKeyDerivation::deriveChildKey,
                        (a, b) -> b);

        return derivedKey.getPrivKeyBytes();
    }

    /**
     * Derives secret from seed
     */
    public static StringSecret deriveSecret(byte[] seed, String keysetId, int counter) {
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
        byte[] entropy = new byte[16]; // 128 bits = 12 słów
        random.nextBytes(entropy);

        return MnemonicCode.INSTANCE.toMnemonic(entropy);
    }

}
