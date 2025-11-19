package com.cashujdk.utils;

import com.cashujdk.cryptography.Cashu;
import com.cashujdk.nut00.*;
import com.cashujdk.nut01.Keyset;
import com.cashujdk.nut01.KeysetId;
import com.cashujdk.nut01.KeysetItemResponse;
import com.cashujdk.nut03.PostSwapResponse;
import com.cashujdk.nut12.DLEQProof;
import com.cashujdk.nut13.Nut13;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class OutputHelper {
    public List<OutputData> createOutputs(long[] amounts, KeysetId keysetId){
        List<OutputData> outputs = new ArrayList<>();
        for (long amount : amounts) {
            var outputData = new OutputData();

            ISecret secret = randomSecret();
            byte[] r = randomPrivKey();

            ECPoint Y = secret.hashToCurve();
            var B_ = Cashu.computeB_(Y, new BigInteger(1, r));

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
            var B_ = Cashu.computeB_(Y, new BigInteger(1, r));

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

    public static List<Proof> constructAndVerifyProofs(
            PostSwapResponse response,
            KeysetItemResponse keyset,
            List<ISecret> secrets,
            List<BigInteger> blindingFactors,
            Consumer<String> callback) throws Exception {
        List<Proof> result = new ArrayList<>();
        for (int i = 0; i < blindingFactors.size(); ++i) {
            BlindSignature signature = response.signatures.get(i);
            BigInteger blindingFactor = blindingFactors.get(i);
            ISecret secret = secrets.get(i);

            ECPoint key = Cashu.hexToPoint(keyset.keys.get(BigInteger.valueOf(signature.amount)));
            ECPoint C = Cashu.computeC(Cashu.hexToPoint(signature.c_), blindingFactor, key);
            if(!Cashu.verifyProof(secret.hashToCurve(), blindingFactor, C, signature.dleq.e, signature.dleq.s, key)){
                callback.accept(signature.c_);
            }
            result.add(
                    new Proof(
                            signature.amount,
                            signature.keysetId,
                            secret,
                            Cashu.pointToHex(C, true),
                            Optional.empty(),
                            Optional.of(
                                    new DLEQProof(signature.dleq.s, signature.dleq.e, Optional.of(signature.dleq.r))
                            )
                    )
            );
        }
        return result;
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
