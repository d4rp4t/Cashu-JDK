package com.cashujdk.serialization;

import com.cashujdk.nut00.InnerToken;
import com.cashujdk.nut00.Proof;
import com.cashujdk.nut00.Token;
import com.cashujdk.nut18.Nut10Option;
import com.cashujdk.nut18.PaymentRequest;
import com.cashujdk.nut18.Transport;
import com.cashujdk.nut18.TransportTag;
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

    // -- Token --

    public byte[] toCBOR(Token token) {
        try {
            // Create a map of proofs grouped by keyset ID
            Map<String, List<Proof>> groupedProofs = new LinkedHashMap<>();
            for (InnerToken entry : token.tokens) {
                for (Proof proof : entry.proofs) {
                    groupedProofs.computeIfAbsent(proof.keysetId, k -> new ArrayList<>()).add(proof);
                }
            }

            // Create the main token array containing proof sets
            CBORObject tokenSets = CBORObject.NewArray();

            // Create each proof set in the order of keyset IDs
            for (Map.Entry<String, List<Proof>> group : groupedProofs.entrySet()) {
                CBORObject proofSet = CBORObject.NewMap();
                proofSet.Add("i", CBORObject.FromObject(Hex.decode(group.getKey())));
                
                // Add proofs array
                CBORObject proofsArray = CBORObject.NewArray();
                for (Proof proof : group.getValue()) {
                    proofsArray.Add(createProofItem(proof));
                }
                proofSet.Add("p", proofsArray);
                
                tokenSets.Add(proofSet);
            }

            // Create the main token object with the canonical order of fields
            CBORObject tokenObj = CBORObject.NewMap();
            
            // Add optional memo field first if present
            if (token.memo != null && !token.memo.isEmpty()) {
                tokenObj.Add("d", CBORObject.FromObject(token.memo));
            }
            
            // Add required fields in lexicographical order
            tokenObj.Add("m", CBORObject.FromObject(token.mint));
            tokenObj.Add("t", tokenSets);
            tokenObj.Add("u", CBORObject.FromObject(token.unit));

            return tokenObj.EncodeToBytes(encodeOptions);

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to CBOR", e);
        }
    }

    private CBORObject createProofItem(Proof proof) throws IOException {
        CBORObject proofMap = CBORObject.NewMap();

        // Add proof fields in lexicographical order
        proofMap.Add("a", CBORObject.FromObject(proof.amount));
        proofMap.Add("c", CBORObject.FromObject(Hex.decode(proof.c)));

        // Add secret data
        if (proof.secret != null) {
            ObjectMapper mapper = new ObjectMapper();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mapper.writeValue(baos, proof.secret);
            byte[] jsonBytes = baos.toByteArray();
            CBORObject secretCbor = CBORObject.FromJSONBytes(jsonBytes);
            proofMap.Add("s", secretCbor);
        }

        // Add optional DLEQ proof if present
        if (proof.dleq != null) {
            CBORObject dleqMap = CBORObject.NewMap();
            dleqMap.Add("e", CBORObject.FromObject(proof.dleq.e.toByteArray()));
            dleqMap.Add("r", CBORObject.FromObject(proof.dleq.r.toByteArray()));
            dleqMap.Add("s", CBORObject.FromObject(proof.dleq.s.toByteArray()));
            proofMap.Add("d", dleqMap);
        }

        // Add optional witness if present
        if (proof.witness != null && !proof.witness.isEmpty()) {
            proofMap.Add("w", CBORObject.FromObject(proof.witness));
        }

        return proofMap;
    }

    // -- Payment request --

    public byte[] toCBOR(PaymentRequest request) {
        CBORObject reqObj = CBORObject.NewOrderedMap();
        request.id.ifPresent(string -> reqObj.Add("i", CBORObject.FromObject(string)));
        request.amount.ifPresent(aLong -> reqObj.Add("a", CBORObject.FromObject(aLong)));
        request.unit.ifPresent(unit->reqObj.Add("u", CBORObject.FromObject(unit)));
        request.singleUse.ifPresent(singleUse->reqObj.Add("s", singleUse));

        if (request.mints.isPresent()) {
            var mints = request.mints.get();
            CBORObject mintArr = CBORObject.NewArray();
            for (String mint : mints) {
                mintArr.Add(CBORObject.FromObject(mint));
            }
            reqObj.Add("m", mintArr);
        }
        request.description.ifPresent(
                description -> reqObj.Add("d", CBORObject.FromObject(description))
        );

        request.transport.ifPresent(transports -> {
            CBORObject transportsArray = CBORObject.NewArray();
            for (Transport t : transports) {
                transportsArray.Add(encodeTransport(t));
            }
            reqObj.Add("t", transportsArray);
        });

       request.nut10Option.ifPresent(option -> reqObj.Add("nut10", encodeNut10Option(option)));

       return reqObj.EncodeToBytes();
    }

    private CBORObject encodeTransport(Transport transport){
        CBORObject transportObj = CBORObject.NewOrderedMap();
        transportObj.Add("t", CBORObject.FromObject(transport.type));
        transportObj.Add("a", CBORObject.FromObject(transport.target));
        transport.tags.ifPresent(tags -> transportObj.Add("g", encodeTags(tags)));
        return transportObj;
    }

    private CBORObject encodeNut10Option(Nut10Option option){
        CBORObject optionObj = CBORObject.NewOrderedMap();
        optionObj.Add("k", option.kind);
        optionObj.Add("d", option.data);
        option.tags.ifPresent(tags -> optionObj.Add("t", encodeTags(tags)));
        return optionObj;
    }

    private CBORObject encodeTags(TransportTag[] tags){
        var tagsObj = CBORObject.NewArray();
        for (TransportTag tag : tags){
            var a = CBORObject.NewArray();
            a.Add(CBORObject.FromObject(tag.key));
            a.Add(CBORObject.FromObject(tag.value));
            tagsObj.Add(a);
        }
        return tagsObj;
    }
}
