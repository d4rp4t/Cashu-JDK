package com.cashujdk.utils;

import com.cashujdk.cryptography.Cashu;
import com.cashujdk.nut00.BlindedMessage;
import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut00.StringSecret;
import com.cashujdk.nut01.Keyset;
import com.cashujdk.nut01.KeysetId;
import com.cashujdk.nut13.Nut13;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class OutputHelper {
    public List<OutputData> createOutputs(long[] amounts, KeysetId keysetId){
        List<OutputData> outputs = new ArrayList<>();
        for (long amount : amounts) {
            var outputData = new OutputData();

            ISecret secret = randomSecret();
            byte[] r = randomPrivKey();

            ECPoint Y = Cashu.hashToCurve(secret.getBytes());
            var B_ = Cashu.computeB_(Y, new BigInteger(r));

            var bm = new BlindedMessage();
            bm.amount = amount;
            bm.keysetId = keysetId.get_id();
            bm.b_ = Cashu.pointToHex(B_, true);

            outputData.secret = secret;
            outputData.blindingFactor = (r);
            outputData.blindedMessage = bm;
            outputs.add(outputData);
        }

        return outputs;
    }

    public List<OutputData> createOutputs(long[] amounts, KeysetId keysetId, List<String> mnemonic, int counter) throws Exception {
        List<OutputData> outputs = new ArrayList<>();

        for (long amount : amounts) {
            var outputData = new OutputData();

            ISecret secret = new StringSecret(Nut13.deriveSecret(mnemonic, keysetId.get_id(), counter).getSecret());
            byte[] r = Nut13.deriveBlindingFactor(mnemonic, keysetId.get_id(), counter);

            ECPoint Y = Cashu.hashToCurve(secret.getBytes());
            var B_ = Cashu.computeB_(Y, new BigInteger(r));

            var bm = new BlindedMessage();
            bm.amount = amount;
            bm.keysetId = keysetId.get_id();
            bm.b_ = Cashu.pointToHex(B_, true);

            outputData.secret = secret;
            outputData.blindingFactor = (r);
            outputData.blindedMessage = bm;
            outputs.add(outputData);
            counter++;
        }

        // remember to bump counter after derivation
        return outputs;
    }

    public static List<Long> splitToProofsAmounts(long paymentAmount, Keyset keyset) {
        List<Long> outputAmounts = new ArrayList<>();

        List<BigInteger> possibleValues = new ArrayList<>(keyset.keySet());
        possibleValues.sort(Comparator.reverseOrder());

        for (BigInteger value : possibleValues) {
            if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                continue;
            }

            long denom = value.longValue();

            while (paymentAmount >= denom) {
                outputAmounts.add(denom);
                paymentAmount -= denom;
            }

            if (paymentAmount == 0) {
                break;
            }
        }

        return outputAmounts;
    }

    public static byte[] randomPrivKey() {
        var random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return bytes;
    }

    public static StringSecret randomSecret() {
        byte[] bytes = randomPrivKey();
        return new StringSecret(HexFormat.of().formatHex(bytes));
    }

}
