package com.cashujdk.nut00;

import com.cashujdk.serialization.CBORSerializer;
import com.cashujdk.serialization.CBORDeserializer;
import com.cashujdk.nut12.DLEQProof;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TokenSerializationTest {

    @Test
    public void testTokenV4VectorSerialization() throws Exception {
        // Test vector from the Cashu NUT-00 specification
        // This is V4 test vector that matches the spec's structure
        Token token = new Token();
        token.mint = "https://8333.space:3338";
        token.unit = "sat";
        
        // Create proof with test vector data
        Proof proof = new Proof();
        proof.keysetId = "01";
        proof.amount = 21;
        proof.c = "0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798";
        
        // Create secret data
        StringSecret secret = new StringSecret("test_secret");
        proof.secret = secret;

        // Create the inner token structure
        InnerToken innerToken = new InnerToken();
        innerToken.keysetId = proof.keysetId;
        innerToken.proofs = List.of(proof);
        
        token.tokens = List.of(innerToken);
        
        // Encode the token
        String encodedToken = token.encode();
        
        // Verify it starts with cashuB
        assertTrue(encodedToken.startsWith("cashuB"));
        
        // Decode and verify fields
        Token decodedToken = Token.decode(encodedToken);
        assertEquals("https://8333.space:3338", decodedToken.mint);
        assertEquals("sat", decodedToken.unit);
        assertEquals(1, decodedToken.tokens.size());
        
        // Verify proof structure
        InnerToken decodedInnerToken = decodedToken.tokens.get(0);
        assertEquals("01", decodedInnerToken.keysetId);
        Proof decodedProof = decodedInnerToken.proofs.get(0);
        assertEquals(21, decodedProof.amount);
        assertEquals(proof.c, decodedProof.c);
        assertEquals("test_secret", ((StringSecret)decodedProof.secret).getSecret());
    }

    @Test
    public void testTokenV4ProofStructure() throws Exception {
        // Create a token with multiple proofs in different keysets
        Token token = new Token();
        token.mint = "https://8333.space:3338";
        token.unit = "sat";
        token.memo = "test memo";
        
        List<InnerToken> tokens = new ArrayList<>();
        
        // Create first proof set with keyset 01
        Proof proof1 = new Proof();
        proof1.keysetId = "01";
        proof1.amount = 21;
        proof1.c = "0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798";
        StringSecret secret1 = new StringSecret("secret1");
        proof1.secret = secret1;
        
        InnerToken innerToken1 = new InnerToken();
        innerToken1.keysetId = "01";
        innerToken1.proofs = List.of(proof1);
        tokens.add(innerToken1);
        
        // Create second proof set with keyset 02
        Proof proof2 = new Proof();
        proof2.keysetId = "02";
        proof2.amount = 42;
        proof2.c = "02c6047f9441ed7d6d3045406e95c07cd85c778e4b8cef3ca7abac09b95c709ee5";
        StringSecret secret2 = new StringSecret("secret2");
        proof2.secret = secret2;
        proof2.witness = "test witness";
        
        InnerToken innerToken2 = new InnerToken();
        innerToken2.keysetId = "02";
        innerToken2.proofs = List.of(proof2);
        tokens.add(innerToken2);
        
        token.tokens = tokens;
        
        // Serialize and deserialize
        String encodedToken = token.encode();
        Token decodedToken = Token.decode(encodedToken);
        
        // Verify structure preservation
        assertEquals(token.mint, decodedToken.mint);
        assertEquals(token.unit, decodedToken.unit);
        assertEquals(token.memo, decodedToken.memo);
        assertEquals(token.tokens.size(), decodedToken.tokens.size());
        
        // Verify first proof set
        InnerToken decodedInnerToken1 = decodedToken.tokens.get(0);
        assertEquals("01", decodedInnerToken1.keysetId);
        assertEquals(21, decodedInnerToken1.proofs.get(0).amount);
        assertEquals("secret1", ((StringSecret)decodedInnerToken1.proofs.get(0).secret).getSecret());
        
        // Verify second proof set
        InnerToken decodedInnerToken2 = decodedToken.tokens.get(1);
        assertEquals("02", decodedInnerToken2.keysetId);
        assertEquals(42, decodedInnerToken2.proofs.get(0).amount);
        assertEquals("secret2", ((StringSecret)decodedInnerToken2.proofs.get(0).secret).getSecret());
        assertEquals("test witness", decodedInnerToken2.proofs.get(0).witness);
    }

    @Test
    public void testTokenV4Base64Variants() throws Exception {
        // Create a token
        Token token = new Token();
        token.mint = "https://8333.space:3338";
        token.unit = "sat";
        
        Proof proof = new Proof();
        proof.keysetId = "01";
        proof.amount = 21;
        proof.c = "0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798";
        proof.secret = new StringSecret("test_secret");
        
        InnerToken innerToken = new InnerToken();
        innerToken.keysetId = proof.keysetId;
        innerToken.proofs = List.of(proof);
        token.tokens = List.of(innerToken);
        
        // Get standard Base64 token
        String encodedToken = token.encode();
        
        // Convert to URL-safe Base64
        String urlSafeToken = "cashuB" + Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(Base64.getDecoder().decode(encodedToken.substring(6)));
        
        // Both should decode correctly
        Token standardDecoded = Token.decode(encodedToken);
        Token urlSafeDecoded = Token.decode(urlSafeToken);
        
        // Verify both decode to the same token
        assertEquals(standardDecoded.mint, urlSafeDecoded.mint);
        assertEquals(standardDecoded.unit, urlSafeDecoded.unit);
        assertEquals(standardDecoded.tokens.size(), urlSafeDecoded.tokens.size());
        
        // Verify proof details match
        Proof standardProof = standardDecoded.tokens.get(0).proofs.get(0);
        Proof urlSafeProof = urlSafeDecoded.tokens.get(0).proofs.get(0);
        
        assertEquals(standardProof.amount, urlSafeProof.amount);
        assertEquals(standardProof.c, urlSafeProof.c);
        assertEquals(
            ((StringSecret)standardProof.secret).getSecret(),
            ((StringSecret)urlSafeProof.secret).getSecret()
        );
    }
}
