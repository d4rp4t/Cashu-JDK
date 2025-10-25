package com.cashujdk.nut00;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class InnerTokenTest {
    
    private static class TestSecret implements ISecret {
        private final byte[] bytes;
        
        public TestSecret() {
            this.bytes = new byte[32]; // Empty 32-byte array for testing
        }
        
        @Override
        public byte[] getBytes() {
            return bytes;
        }
        
        @Override
        public ECPoint toCurve() {
            return null; // Not needed for this test
        }
    }

    @Test
    public void testKeysetIdTrimming() {
        // Test with v2 keyset ID (starting with "01")
        String fullKeysetIdStr = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        ArrayList<String> fullKeysetId = new ArrayList<>(Arrays.asList(fullKeysetIdStr));
        
        Proof proof = new Proof();
        proof.keysetId = fullKeysetId.get(0);
        proof.amount = 100;
        proof.secret = new TestSecret();
        
        // Create inner token - this should trigger ID trimming
        InnerToken innerToken = new InnerToken(fullKeysetId.get(0), List.of(proof));
        
        // Verify the ID was trimmed to 16 characters
        String expectedTrimmedId = "0123456789abcdef";
        assertEquals(expectedTrimmedId, innerToken.keysetId);
    }
    
    @Test
    public void testKeysetIdRehydration() {
        // Setup a trimmed keyset ID
        String trimmedId = "0123456789abcdef";
        String fullKeysetId = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        
        // Create proof and inner token with trimmed ID
        Proof proof = new Proof();
        proof.keysetId = trimmedId;
        proof.amount = 100;
        proof.secret = new TestSecret();
        
        InnerToken innerToken = new InnerToken(trimmedId, List.of(proof));
        
        // Test rehydration
        List<String> fullKeysetIds = Arrays.asList(fullKeysetId);
        List<Proof> rehydratedProofs = innerToken.getProofs(fullKeysetIds);
        
        // Verify proofs were rehydrated with full keyset ID
        assertNotNull(rehydratedProofs);
        assertEquals(1, rehydratedProofs.size());
        assertEquals(fullKeysetId, rehydratedProofs.get(0).keysetId);
    }
    
    @Test
    public void testKeepOriginalV1KeysetId() {
        // Test with v1 keyset ID (shorter format)
        String v1KeysetId = "00abcdefabcdefab";
        
        Proof proof = new Proof();
        proof.keysetId = v1KeysetId;
        proof.amount = 100;
        proof.secret = new TestSecret();
        
        // Create inner token
        InnerToken innerToken = new InnerToken(v1KeysetId, List.of(proof));
        
        // Verify the ID was not changed
        assertEquals(v1KeysetId, innerToken.keysetId);
        assertEquals(v1KeysetId, innerToken.getProofs(Arrays.asList(v1KeysetId)).get(0).keysetId);
    }
}
