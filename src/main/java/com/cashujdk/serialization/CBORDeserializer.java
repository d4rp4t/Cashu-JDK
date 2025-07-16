package com.cashujdk.serialization;

import com.cashujdk.nut00.InnerToken;
import com.cashujdk.nut00.Proof;
import com.cashujdk.nut00.Token;
import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut12.DLEQProof;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import org.bouncycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CBORDeserializer {

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
            
            proof.dleq = new DLEQProof(s, e, r);
        }

        // Parse optional witness
        if (proofObj.ContainsKey("w")) {
            proof.witness = proofObj.get("w").AsString();
        }

        return proof;
    }
}
