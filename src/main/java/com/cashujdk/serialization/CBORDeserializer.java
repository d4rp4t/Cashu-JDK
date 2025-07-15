package com.cashujdk.serialization;

import com.cashujdk.nut00.InnerToken;
import com.cashujdk.nut00.Proof;
import com.cashujdk.nut00.Token;
import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut00.StringSecret;
import com.cashujdk.nut12.DLEQProof;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORObject;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CBORDeserializer {

    private final ECParameterSpec CURVE_PARAMS;

    public CBORDeserializer() {
        ECNamedCurveParameterSpec params = ECNamedCurveTable.getParameterSpec("secp256k1");
        this.CURVE_PARAMS = params;
    }

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
        token.mint = cborObject.get("m").AsString();
        token.unit = cborObject.get("u").AsString();
        
        // Parse optional memo
        if (cborObject.ContainsKey("d")) {
            token.memo = cborObject.get("d").AsString();
        }

        // Parse token sets
        CBORObject tokenSets = cborObject.get("t");
        token.tokens = new ArrayList<>();

        for (CBORObject proofSet : tokenSets.getValues()) {
            String keysetId = Hex.toHexString(proofSet.get("i").GetByteString());
            CBORObject proofsArray = proofSet.get("p");
            
            List<Proof> proofs = new ArrayList<>();
            for (CBORObject proofObj : proofsArray.getValues()) {
                proofs.add(parseProof(proofObj, keysetId));
            }

            // Create an InnerToken for each proof
            for (Proof proof : proofs) {
                InnerToken innerToken = new InnerToken();
                innerToken.proofs = List.of(proof);
                token.tokens.add(innerToken);
            }
        }

        return token;
    }

    private Proof parseProof(CBORObject proofObj, String keysetId) throws Exception {
        Proof proof = new Proof();
        proof.keysetId = keysetId;
        proof.amount = proofObj.get("a").AsNumber().ToInt32Checked();

        // Parse secret
        CBORObject secretObj = proofObj.get("s");
        String secretStr = secretObj.ToJSONString();
        // Using SecretDeserializer indirectly through ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        proof.secret = mapper.readValue(secretStr, ISecret.class);

        // Parse C point
        byte[] cBytes = proofObj.get("c").GetByteString();
        proof.c = CURVE_PARAMS.getCurve().decodePoint(cBytes);

        // Parse optional DLEQ
        if (proofObj.ContainsKey("d")) {
            CBORObject dleqObj = proofObj.get("d");
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
