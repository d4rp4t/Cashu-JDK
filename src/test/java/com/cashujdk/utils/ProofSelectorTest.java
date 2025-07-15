package com.cashujdk.utils;

import com.cashujdk.errors.CashuExceptions;
import com.cashujdk.nut00.Proof;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProofSelectorTest {

    private ProofSelector proofSelector;
    private Map<String, Integer> keysetFees;

    @BeforeEach
    void setUp() {
        // Setup keyset fees for testing
        keysetFees = new HashMap<>();
        keysetFees.put("keyset1", 1000); // 1 sat per key
        keysetFees.put("keyset2", 2000); // 2 sats per key
        proofSelector = new ProofSelector(Optional.of(keysetFees));
    }

    private Proof createProof(long amount, String keysetId) {
        Proof proof = new Proof();
        proof.amount = amount;
        proof.keysetId = keysetId;
        return proof;
    }

    private List<Proof> createProofsList(Proof... proofs) {
        return new ArrayList<>(Arrays.asList(proofs));
    }

    @Test
    @DisplayName("Test basic proof selection without fees")
    void testBasicProofSelection() {
        List<Proof> proofs = createProofsList(
            createProof(10L, "keyset1"),
            createProof(20L, "keyset1"),
            createProof(30L, "keyset1")
        );
        
        // Test selecting exactly 30
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(proofs, 30, false);
        assertNotNull(result);
        assertEquals(30L, result.getSecond().stream().mapToLong(p -> p.amount).sum());
        
        // Test remaining proofs have correct amount
        long remainingAmount = result.getFirst().stream().mapToLong(p -> p.amount).sum();
        assertEquals(30L, remainingAmount);
    }

    @Test
    @DisplayName("Test proof selection with fees")
    void testProofSelectionWithFees() {
        List<Proof> proofs = createProofsList(
            createProof(10L, "keyset1"),
            createProof(20L, "keyset1"),
            createProof(30L, "keyset1")
        );
        
        // Test selecting with fees (each proof has 1 sat fee from keyset1)
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(proofs, 28, true);
        assertNotNull(result);
        
        // Verify selected amount covers requested amount plus fees
        long selectedAmount = result.getSecond().stream().mapToLong(p -> p.amount).sum();
        long feeAmount = result.getSecond().size(); // 1 sat per proof
        assertTrue(selectedAmount - feeAmount >= 28L);
    }

    @Test
    @DisplayName("Test empty proofs list")
    void testEmptyProofs() {
        List<Proof> emptyProofs = new ArrayList<>();
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(emptyProofs, 10, false);
        
        assertNotNull(result);
        assertTrue(result.getFirst().isEmpty());
        assertTrue(result.getSecond().isEmpty());
    }

    @Test
    @DisplayName("Test insufficient funds")
    void testInsufficientFunds() {
        List<Proof> proofs = createProofsList(
            createProof(10L, "keyset1"),
            createProof(20L, "keyset1")
        );
        
        // Try to select more than available
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(proofs, 50, false);
        
        assertNotNull(result);
        assertEquals(proofs, result.getFirst());
        assertTrue(result.getSecond().isEmpty());
    }

    @Test
    @DisplayName("Test selection with different keyset fees")
    void testDifferentKeysetFees() {
        List<Proof> proofs = createProofsList(
            createProof(10L, "keyset1"), // 1 sat fee
            createProof(20L, "keyset2"), // 2 sat fee
            createProof(30L, "keyset1")  // 1 sat fee
        );
        
        // Test selecting with different fees
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(proofs, 25, true);
        assertNotNull(result);
        
        // Verify selected amount covers requested amount plus respective fees
        long selectedAmount = result.getSecond().stream().mapToLong(p -> p.amount).sum();
        long totalFees = result.getSecond().stream()
            .mapToLong(p -> keysetFees.get(p.keysetId) / 1000L)
            .sum();
            
        assertTrue(selectedAmount - totalFees >= 25L);
    }

    @Test
    @DisplayName("Test invalid amount request")
    void testInvalidAmount() {
        List<Proof> proofs = createProofsList(
            createProof(10L, "keyset1"),
            createProof(20L, "keyset1")
        );
        
        // Test with negative amount
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(proofs, -10, false);
        
        assertNotNull(result);
        assertEquals(proofs, result.getFirst());
        assertTrue(result.getSecond().isEmpty());
    }

    @Test
    @DisplayName("Test proofs with zero amounts")
    void testZeroAmountProofs() {
        List<Proof> proofs = createProofsList(
            createProof(0L, "keyset1"),
            createProof(10L, "keyset1"),
            createProof(0L, "keyset1")
        );
        
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(proofs, 10, true);
        
        assertNotNull(result);
        System.out.println("Selected proofs: " + result.getSecond());
        System.out.println("Selected amounts: " + result.getSecond().stream().map(p -> p.amount).toList());
        System.out.println("Remaining proofs: " + result.getFirst());
        System.out.println("Remaining amounts: " + result.getFirst().stream().map(p -> p.amount).toList());
        // Check that zero amount proofs are not selected
        for (Proof p : result.getSecond()) {
            assertNotEquals(0L, p.amount);
        }
        // Check that total amount matches expected
        assertEquals(10L, result.getSecond().stream().mapToLong(p -> p.amount).sum());
        // Verify all zero amount proofs are kept
        long zeroAmountProofsInKeep = result.getFirst().stream()
            .filter(p -> p.amount == 0L)
            .count();
        assertEquals(2L, zeroAmountProofsInKeep);
    }

    @Test
    @DisplayName("Test for exact match selection")
    void testExactMatchSelection() {
        List<Proof> proofs = createProofsList(
            createProof(5L, "keyset1"),
            createProof(7L, "keyset1"),
            createProof(8L, "keyset1"),
            createProof(10L, "keyset1")
        );
        
        // Try to select exactly 15 (should find 7+8)
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(proofs, 15, false);
        
        assertNotNull(result);
        assertEquals(15L, result.getSecond().stream().mapToLong(p -> p.amount).sum());
        assertEquals(2, result.getSecond().size());
    }

    @Test
    @DisplayName("Test large number of proofs")
    void testLargeNumberOfProofs() {
        List<Proof> proofs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            proofs.add(createProof(i + 1L, "keyset1"));
        }
        
        // Should complete within reasonable time
        long startTime = System.currentTimeMillis();
        Pair<List<Proof>, List<Proof>> result = proofSelector.selectProofsToSend(proofs, 500, true);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(result);
        assertTrue((endTime - startTime) <= 2000); // Should complete within 2 seconds
        assertTrue(result.getSecond().stream().mapToLong(p -> p.amount).sum() >= 500L);
    }
}
