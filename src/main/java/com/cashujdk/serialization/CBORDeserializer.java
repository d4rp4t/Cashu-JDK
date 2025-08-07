package com.cashujdk.serialization;

import com.cashujdk.nut00.InnerToken;
import com.cashujdk.nut00.Proof;
import com.cashujdk.nut00.Token;
import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut12.DLEQProof;
import com.cashujdk.nut18.Nut10Option;
import com.cashujdk.nut18.PaymentRequest;
import com.cashujdk.nut18.Transport;
import com.cashujdk.nut18.TransportTag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CBORDeserializer {

    // -- Token --

    public Token fromCBOR(byte[] cbor) {
        try {
            CBORObject cborObject = CBORObject.DecodeFromBytes(cbor);
            return parseToken(cborObject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize from CBOR", e);
        }
    }

    private Token parseToken(CBORObject cborObject) throws Exception {
        Token token = new Token();
        
        // Parse required fields
        if (!cborObject.ContainsKey("m") || !cborObject.ContainsKey("u") || !cborObject.ContainsKey("t")) {
            throw new RuntimeException("Missing required token fields");
        }
        
        token.mint = cborObject.get("m").AsString();
        token.unit = cborObject.get("u").AsString();
        
        // Parse optional memo
        if (cborObject.ContainsKey("d")) {
            token.memo = cborObject.get("d").AsString();
        }

        // Parse token sets
        CBORObject tokenSets = cborObject.get("t");
        if (tokenSets.getType() != CBORType.Array) {
            throw new RuntimeException("Token sets must be an array");
        }

        token.tokens = new ArrayList<>();
        
        // Process each proof set
        for (CBORObject proofSet : tokenSets.getValues()) {
            if (!proofSet.ContainsKey("i") || !proofSet.ContainsKey("p")) {
                throw new RuntimeException("Invalid proof set structure");
            }

            String keysetId = Hex.toHexString(proofSet.get("i").GetByteString());
            CBORObject proofsArray = proofSet.get("p");
            
            if (proofsArray.getType() != CBORType.Array) {
                throw new RuntimeException("Proofs must be an array");
            }

            // Create proofs for this keyset
            for (CBORObject proofObj : proofsArray.getValues()) {
                Proof proof = parseProof(proofObj);
                proof.keysetId = keysetId;
                
                // Create an InnerToken for each proof
                InnerToken innerToken = new InnerToken();
                innerToken.keysetId = keysetId;
                innerToken.proofs = List.of(proof);
                token.tokens.add(innerToken);
            }
        }

        return token;
    }

    private Proof parseProof(CBORObject proofObj) throws Exception {
        if (!proofObj.ContainsKey("a") || !proofObj.ContainsKey("c") || !proofObj.ContainsKey("s")) {
            throw new RuntimeException("Invalid proof structure");
        }

        Proof proof = new Proof();
        
        // Parse required fields
        proof.amount = proofObj.get("a").AsNumber().ToInt32Checked();
        proof.c = Hex.toHexString(proofObj.get("c").GetByteString());
        
        // Parse secret
        CBORObject secretObj = proofObj.get("s");
        String secretJson = secretObj.ToJSONString();
        ObjectMapper mapper = new ObjectMapper();
        proof.secret = mapper.readValue(secretJson, ISecret.class);

        // Parse optional DLEQ proof
        if (proofObj.ContainsKey("d")) {
            CBORObject dleqObj = proofObj.get("d");
            if (!dleqObj.ContainsKey("e") || !dleqObj.ContainsKey("s") || !dleqObj.ContainsKey("r")) {
                throw new RuntimeException("Invalid DLEQ proof structure");
            }

            BigInteger e = new BigInteger(dleqObj.get("e").GetByteString());
            BigInteger s = new BigInteger(dleqObj.get("s").GetByteString());
            BigInteger r = new BigInteger(dleqObj.get("r").GetByteString());
            
            proof.dleq = new DLEQProof(s, e, Optional.of(r));
        }

        // Parse optional witness
        if (proofObj.ContainsKey("w")) {
            proof.witness = proofObj.get("w").AsString();
        }

        return proof;
    }


    // -- Payment Request --
    public PaymentRequest prFromCBOR(byte[] cbor){
        try {
            CBORObject cborObject = CBORObject.DecodeFromBytes(cbor);
            return parsePaymentRequest(cborObject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize from CBOR", e);
        }
    }

    private PaymentRequest parsePaymentRequest(CBORObject cborObject) {
        PaymentRequest pr = new PaymentRequest();

        if(cborObject.ContainsKey("i")) {
            pr.id = Optional.of(cborObject.get("i").AsString());
        }
        if(cborObject.ContainsKey("a")) {
            pr.amount = Optional.of(cborObject.get("a").AsInt64Value());
        }
        if(cborObject.ContainsKey("u")) {
            pr.unit = Optional.of(cborObject.get("u").AsString());
        }
        if(cborObject.ContainsKey("s")) {
            pr.singleUse = Optional.of(cborObject.get("s").AsBoolean());
        }
        if(cborObject.ContainsKey("m")) {
            pr.mints = Optional.of(
                    cborObject.get("m").getValues().stream()
                            .map(CBORObject::AsString).toArray(String[]::new)
            );
        }
        if(cborObject.ContainsKey("d")) {
            pr.description = Optional.of(cborObject.get("d").AsString());
        }
        if(cborObject.ContainsKey("t")) {
            pr.transport = Optional.of(
                    decodeTransportArray(cborObject.get("t"))
            );
        }
        if(cborObject.ContainsKey("nut10")) {
            pr.nut10Option = Optional.of(
                    decodeNut10Option(cborObject.get("nut10"))
            );
        }
        return pr;
    }

    private Nut10Option decodeNut10Option(CBORObject optionObj) {
        Nut10Option option = new Nut10Option();

        if (optionObj.ContainsKey("k")) {
            option.kind = optionObj.get("k").AsString();
        }

        if (optionObj.ContainsKey("d")) {
            option.data = optionObj.get("d").AsString();
        }

        if (optionObj.ContainsKey("t")) {
            CBORObject tagsArray = optionObj.get("t");
            if (tagsArray.getType() == CBORType.Array) {
                TransportTag[] tags = tagsArray.getValues().stream()
                        .map(tagArr -> {
                            TransportTag tag = new TransportTag();
                            if (tagArr.getType() == CBORType.Array && tagArr.size() >= 2) {
                                tag.key = tagArr.get(0).AsString();
                                tag.value = tagArr.get(1).AsString();
                            }
                            return tag;
                        })
                        .toArray(TransportTag[]::new);

                option.tags = Optional.of(tags);
            }
        } else {
            option.tags = Optional.empty();
        }

        return option;
    }

    private Transport[] decodeTransportArray(CBORObject transportsArray) {
        return transportsArray.getValues().stream()
                .map(transportObj -> {
                    Transport transport = new Transport();
                    for (CBORObject key : transportObj.getKeys()) {
                        CBORObject val = transportObj.get(key);
                        switch (key.AsString()) {
                            case "t":
                                transport.type = val.AsString();
                                break;
                            case "a":
                                transport.target = val.AsString();
                                break;
                            case "g":
                                transport.tags = Optional.of(val.getValues().stream()
                                        .map(tagArr -> {
                                            TransportTag tag = new TransportTag();
                                            tag.key = tagArr.get(0).AsString();
                                            tag.value = tagArr.get(1).AsString();
                                            return tag;
                                        }).toArray(TransportTag[]::new));
                                break;
                        }
                    }
                    return transport;
                }).toArray(Transport[]::new);
    }


    //{
    //  "i": str <optional>,
    //  "a": int <optional>,
    //  "u": str <optional>,
    //  "s": bool <optional>,
    //  "m": Array[str] <optional>,
    //  "d": str <optional>,
    //  "t": Array[Transport] <optional>,
    //  "nut10": NUT10Option <optional>,
    //}
}
