package com.cashujdk.serialization;

import com.cashujdk.nut00.InnerToken;
import com.cashujdk.nut00.Proof;
import com.cashujdk.nut00.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBOREncodeOptions;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


public class CBORSerializer {

    private final CBOREncodeOptions encodeOptions;

    public CBORSerializer() {
        this.encodeOptions = CBOREncodeOptions.Default;
    }

    public byte[] toCBOR(Token token) {
        try {
            Map<String, List<Proof>> groupedProofs = new LinkedHashMap<>();
            for (InnerToken entry : token.tokens) {
                for (Proof proof : entry.proofs) {
                    groupedProofs.computeIfAbsent(proof.keysetId, k -> new ArrayList<>()).add(proof);
                }
            }

            CBORObject tokenSets = CBORObject.NewArray();

            for (Map.Entry<String, List<Proof>> group : groupedProofs.entrySet()) {
                CBORObject proofSetItem = createProofSetItem(group.getKey(), group.getValue());
                tokenSets.Add(proofSetItem);
            }

            CBORObject cbor = createOrderedCBORObject(tokenSets, token);

            return cbor.EncodeToBytes(encodeOptions);

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to CBOR", e);
        }
    }


    private CBORObject createOrderedCBORObject(CBORObject tokenSets, Token token) {

        Map<CBORObject, CBORObject> orderedMap = new LinkedHashMap<>();

        if (token.memo != null) {
            orderedMap.put(CBORObject.FromObject("d"), CBORObject.FromObject(token.memo));
        }

        orderedMap.put(CBORObject.FromObject("t"), tokenSets);
        orderedMap.put(CBORObject.FromObject("m"), CBORObject.FromObject(token.mint));
        orderedMap.put(CBORObject.FromObject("u"), CBORObject.FromObject(token.unit));

        return CBORObject.FromObject(orderedMap);
    }

    private CBORObject createProofSetItem(String keysetId, List<Proof> proofs) throws IOException {
        Map<CBORObject, CBORObject> proofSetMap = new LinkedHashMap<>();

        proofSetMap.put(CBORObject.FromObject("i"), CBORObject.FromObject(Hex.decode(keysetId)));

        CBORObject proofsArray = CBORObject.NewArray();
        for (Proof proof : proofs) {
            proofsArray.Add(createProofItem(proof));
        }
        proofSetMap.put(CBORObject.FromObject("p"), proofsArray);

        return CBORObject.FromObject(proofSetMap);
    }

    private CBORObject createProofItem(Proof proof) throws IOException {
        Map<CBORObject, CBORObject> proofMap = new LinkedHashMap<>();

        proofMap.put(CBORObject.FromObject("a"), CBORObject.FromObject(proof.amount));

        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, proof.secret);
        byte[] jsonBytes = baos.toByteArray();

        CBORObject secretCbor = CBORObject.FromJSONBytes(jsonBytes);
        proofMap.put(CBORObject.FromObject("s"), secretCbor);

        proofMap.put(CBORObject.FromObject("c"), CBORObject.FromObject(proof.c));

        if (proof.dleq != null) {
            proofMap.put(CBORObject.FromObject("d"), createDLEQObject(proof.dleq));
        }

        if (proof.witness != null) {
            proofMap.put(CBORObject.FromObject("w"), CBORObject.FromObject(proof.witness));
        }

        return CBORObject.FromObject(proofMap);
    }

    private CBORObject createDLEQObject(Object dleq) {
        Map<CBORObject, CBORObject> dleqMap = new LinkedHashMap<>();

        try {
            var eField = dleq.getClass().getField("e");
            var sField = dleq.getClass().getField("s");
            var rField = dleq.getClass().getField("r");

            dleqMap.put(CBORObject.FromObject("e"), CBORObject.FromObject(((java.math.BigInteger) eField.get(dleq)).toByteArray()));
            dleqMap.put(CBORObject.FromObject("s"), CBORObject.FromObject(((java.math.BigInteger) sField.get(dleq)).toByteArray()));
            dleqMap.put(CBORObject.FromObject("r"), CBORObject.FromObject(((java.math.BigInteger) rField.get(dleq)).toByteArray()));

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize DLEQ", e);
        }

        return CBORObject.FromObject(dleqMap);
    }
}
