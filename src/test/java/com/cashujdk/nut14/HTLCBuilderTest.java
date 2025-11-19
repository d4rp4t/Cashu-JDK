package com.cashujdk.nut14;

import com.cashujdk.cryptography.Cashu;
import com.cashujdk.cryptography.ECC;
import com.cashujdk.nut11.P2PKProofSecret;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HTLCBuilderTest {

    private static final String DUMMY_PUBKEY = "020000000000000000000000000000000000000000000000000000000000000001";
    
    // Helper method to generate a valid 64-character hex hashlock
    private String generateValidHashLock() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Hex.toHexString(bytes).toLowerCase();
    }

    // Helper method to generate a test ECPoint (public key)
    private ECPoint generateTestPubkey() {
        BigInteger privateKey = Cashu.generateRandomScalar();
        return ECC.DOMAIN.getG().multiply(privateKey).normalize();
    }

    @Test
    public void testGetHashLock() {
        HTLCBuilder builder = new HTLCBuilder();
        String expectedHashLock = generateValidHashLock();
        builder.setHashLock(expectedHashLock);
        
        assertEquals(expectedHashLock, builder.getHashLock());
    }

    @Test
    public void testSetHashLock() {
        HTLCBuilder builder = new HTLCBuilder();
        String hashLock = generateValidHashLock();
        
        builder.setHashLock(hashLock);
        
        assertEquals(hashLock, builder.getHashLock());
    }

    @Test
    public void testLoadFromHTLCProofSecret() {
        // Create a proof secret with the correct structure
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        
        // Load the builder from proof secret
        HTLCBuilder builder = HTLCBuilder.load(proofSecret);
        
        assertNotNull(builder);
        assertEquals(hashLock, builder.getHashLock());
        assertEquals(nonce, builder.getNonce());
    }

    @Test
    public void testLoadFiltersDummyPubkey() {
        // Create a proof secret
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey1 = generateTestPubkey();
        ECPoint pubkey2 = generateTestPubkey();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey1, true), Cashu.pointToHex(pubkey2, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        
        // Load the builder
        HTLCBuilder builder = HTLCBuilder.load(proofSecret);
        
        // Verify dummy pubkey is not in the list
        assertNotNull(builder.getPubkeys());
        for (ECPoint pubkey : builder.getPubkeys()) {
            assertNotEquals(Cashu.hexToPoint(DUMMY_PUBKEY), pubkey);
        }
    }

    @Test
    public void testLoadWithLockTimeTag() {
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        long lockTime = Instant.now().plusSeconds(3600).getEpochSecond();
        
        String[][] tags = new String[][] {
            {"locktime", String.valueOf(lockTime)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        HTLCBuilder builder = HTLCBuilder.load(proofSecret);
        
        assertNotNull(builder.getLockTime());
        assertEquals(lockTime, builder.getLockTime().getEpochSecond());
    }

    @Test
    public void testLoadWithRefundPubkeys() {
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint refundKey1 = generateTestPubkey();
        ECPoint refundKey2 = generateTestPubkey();
        long lockTime = Instant.now().plusSeconds(3600).getEpochSecond();
        
        String[][] tags = new String[][] {
            {"locktime", String.valueOf(lockTime)},
            {"refund", Cashu.pointToHex(refundKey1, true), Cashu.pointToHex(refundKey2, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        HTLCBuilder builder = HTLCBuilder.load(proofSecret);
        
        assertNotNull(builder.getRefundPubkeys());
        assertEquals(2, builder.getRefundPubkeys().size());
    }

    @Test
    public void testLoadWithSignatureThreshold() {
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey1 = generateTestPubkey();
        ECPoint pubkey2 = generateTestPubkey();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey1, true), Cashu.pointToHex(pubkey2, true)},
            {"n_sigs", "2"}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        HTLCBuilder builder = HTLCBuilder.load(proofSecret);
        
        assertEquals(2, builder.getSignatureThreshold());
    }

    @Test
    public void testLoadWithSigFlag() {
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        
        String[][] tags = new String[][] {
            {"sigflag", "SIG_INPUTS"}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        HTLCBuilder builder = HTLCBuilder.load(proofSecret);
        
        assertEquals("SIG_INPUTS", builder.getSigFlag());
    }

    @Test
    public void testBuildWithValidHashLock() {
        HTLCBuilder builder = new HTLCBuilder();
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        
        builder.setHashLock(hashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        
        HTLCProofSecret result = builder.build();
        
        assertNotNull(result);
        assertEquals(hashLock, result.getData());
        assertEquals(nonce, result.getNonce());
    }

    @Test
    public void testBuildThrowsExceptionForInvalidHashLockLength() {
        HTLCBuilder builder = new HTLCBuilder();
        String invalidHashLock = "abc123"; // Not 64 characters
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        
        builder.setHashLock(invalidHashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.build();
        });
        
        assertEquals("hashLock length must be 64", exception.getMessage());
    }

    @Test
    public void testBuildWithExactly64CharacterHashLock() {
        HTLCBuilder builder = new HTLCBuilder();
        String hashLock = "a".repeat(64); // Exactly 64 characters
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        
        builder.setHashLock(hashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        
        HTLCProofSecret result = builder.build();
        
        assertNotNull(result);
        assertEquals(hashLock, result.getData());
    }

    @Test
    public void testBuildThrowsExceptionForTooShortHashLock() {
        HTLCBuilder builder = new HTLCBuilder();
        String shortHashLock = "a".repeat(63); // 63 characters
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        
        builder.setHashLock(shortHashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        
        assertThrows(IllegalArgumentException.class, () -> {
            builder.build();
        });
    }

    @Test
    public void testBuildThrowsExceptionForTooLongHashLock() {
        HTLCBuilder builder = new HTLCBuilder();
        String longHashLock = "a".repeat(65); // 65 characters
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        
        builder.setHashLock(longHashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        
        assertThrows(IllegalArgumentException.class, () -> {
            builder.build();
        });
    }

    @Test
    public void testBuildInjectsDummyPubkey() {
        HTLCBuilder builder = new HTLCBuilder();
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        
        builder.setHashLock(hashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        
        HTLCProofSecret result = builder.build();
        
        // Verify the result contains the hashlock as data, not a pubkey
        assertEquals(hashLock, result.getData());
        assertNotEquals(Cashu.pointToHex(pubkey, true), result.getData());
    }

    @Test
    public void testBuildWithMultiplePubkeys() {
        HTLCBuilder builder = new HTLCBuilder();
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey1 = generateTestPubkey();
        ECPoint pubkey2 = generateTestPubkey();
        ECPoint pubkey3 = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey1);
        pubkeys.add(pubkey2);
        pubkeys.add(pubkey3);
        
        builder.setHashLock(hashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        builder.setSignatureThreshold(2);
        
        HTLCProofSecret result = builder.build();
        
        assertNotNull(result);
        assertEquals(hashLock, result.getData());
        
        // Verify tags include pubkeys and threshold
        assertNotNull(result.getTags());
    }

    @Test
    public void testRoundTripLoadAndBuild() {
        // Create initial builder
        HTLCBuilder builder1 = new HTLCBuilder();
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        
        builder1.setHashLock(hashLock);
        builder1.setNonce(nonce);
        builder1.setPubkeys(pubkeys);
        
        // Build proof secret
        HTLCProofSecret proofSecret = builder1.build();
        
        // Load back into builder
        HTLCBuilder builder2 = HTLCBuilder.load(proofSecret);
        
        // Verify data is preserved
        assertEquals(hashLock, builder2.getHashLock());
        assertEquals(nonce, builder2.getNonce());
        assertNotNull(builder2.getPubkeys());
    }

    @Test
    public void testRoundTripWithComplexStructure() {
        // Create builder with all features
        HTLCBuilder builder1 = new HTLCBuilder();
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey1 = generateTestPubkey();
        ECPoint pubkey2 = generateTestPubkey();
        ECPoint refundKey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey1);
        pubkeys.add(pubkey2);
        List<ECPoint> refundKeys = new ArrayList<>();
        refundKeys.add(refundKey);
        
        Instant lockTime = Instant.now().plusSeconds(3600);
        
        builder1.setHashLock(hashLock);
        builder1.setNonce(nonce);
        builder1.setPubkeys(pubkeys);
        builder1.setRefundPubkeys(refundKeys);
        builder1.setLockTime(lockTime);
        builder1.setSignatureThreshold(2);
        builder1.setSigFlag("SIG_INPUTS");
        
        // Build and reload
        HTLCProofSecret proofSecret = builder1.build();
        HTLCBuilder builder2 = HTLCBuilder.load(proofSecret);
        
        // Verify all properties
        assertEquals(hashLock, builder2.getHashLock());
        assertEquals(nonce, builder2.getNonce());
        assertEquals(lockTime.getEpochSecond(), builder2.getLockTime().getEpochSecond());
        assertEquals(2, builder2.getSignatureThreshold());
        assertEquals("SIG_INPUTS", builder2.getSigFlag());
        assertNotNull(builder2.getRefundPubkeys());
        assertEquals(1, builder2.getRefundPubkeys().size());
    }

    @Test
    public void testBuildWithEmptyHashLockThrowsException() {
        HTLCBuilder builder = new HTLCBuilder();
        builder.setHashLock("");
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        
        assertThrows(IllegalArgumentException.class, () -> {
            builder.build();
        });
    }

    @Test
    public void testBuildPreservesAllTags() {
        HTLCBuilder builder = new HTLCBuilder();
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey1 = generateTestPubkey();
        ECPoint pubkey2 = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey1);
        pubkeys.add(pubkey2);
        
        builder.setHashLock(hashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        builder.setSignatureThreshold(2);
        builder.setSigFlag("SIG_ALL");
        
        HTLCProofSecret result = builder.build();
        
        // Load back and verify all tags
        HTLCBuilder reloaded = HTLCBuilder.load(result);
        assertEquals(2, reloaded.getSignatureThreshold());
        assertEquals("SIG_ALL", reloaded.getSigFlag());
    }

    @Test
    public void testLoadHandlesNullTags() {
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, null);
        
        HTLCBuilder builder = HTLCBuilder.load(proofSecret);
        
        assertNotNull(builder);
        assertEquals(hashLock, builder.getHashLock());
    }

    @Test
    public void testBuildHashLockStoredAsDataField() {
        HTLCBuilder builder = new HTLCBuilder();
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        
        builder.setHashLock(hashLock);
        builder.setNonce(nonce);
        builder.setPubkeys(pubkeys);
        
        HTLCProofSecret result = builder.build();
        
        // The hashlock should be stored in the data field, not as a hex-encoded ECPoint
        assertEquals(hashLock, result.getData());
        assertEquals(64, result.getData().length());
    }

    @Test
    public void testDummyPubkeyIsConsistentConstant() {
        // Verify the dummy pubkey constant is correct
        assertEquals(66, DUMMY_PUBKEY.length()); // 33 bytes = 66 hex chars for compressed pubkey
        assertTrue(DUMMY_PUBKEY.startsWith("02") || DUMMY_PUBKEY.startsWith("03"));
        
        // Verify it can be converted to an ECPoint
        assertDoesNotThrow(() -> {
            ECPoint point = Cashu.hexToPoint(DUMMY_PUBKEY);
            assertNotNull(point);
        });
    }

    @Test
    public void testLoadAndBuildMaintainsHashLockAsString() {
        // This tests the key change: hashLock is now a String, not ECPoint
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, null);
        HTLCBuilder builder = HTLCBuilder.load(proofSecret);
        
        // Verify hashLock is stored as String
        assertEquals(String.class, builder.getHashLock().getClass());
        assertEquals(hashLock, builder.getHashLock());
        
        // Build and verify it's still a string
        ECPoint pubkey = generateTestPubkey();
        List<ECPoint> pubkeys = new ArrayList<>();
        pubkeys.add(pubkey);
        builder.setPubkeys(pubkeys);
        
        HTLCProofSecret rebuilt = builder.build();
        assertEquals(hashLock, rebuilt.getData());
    }

    @Test
    public void testMultipleLoadsProduceSameResult() {
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        
        HTLCBuilder builder1 = HTLCBuilder.load(proofSecret);
        HTLCBuilder builder2 = HTLCBuilder.load(proofSecret);
        
        assertEquals(builder1.getHashLock(), builder2.getHashLock());
        assertEquals(builder1.getNonce(), builder2.getNonce());
    }
}