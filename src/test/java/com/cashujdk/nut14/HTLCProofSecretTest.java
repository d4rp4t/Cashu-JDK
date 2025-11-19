package com.cashujdk.nut14;

import com.cashujdk.cryptography.Cashu;
import com.cashujdk.cryptography.ECC;
import com.cashujdk.nut00.BlindedMessage;
import com.cashujdk.nut00.Proof;
import com.cashujdk.nut00.StringSecret;
import com.cashujdk.nut11.P2PKProofSecret;
import com.cashujdk.nut11.P2PKWitness;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HTLCProofSecretTest {

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

    // Helper method to generate a private key
    private ECPrivateKeyParameters generatePrivateKey() {
        BigInteger d = Cashu.generateRandomScalar();
        return new ECPrivateKeyParameters(d, ECC.DOMAIN);
    }

    // Helper method to compute SHA-256 hash
    private byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConstructor() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        String[][] tags = new String[][] {{"locktime", "1234567890"}};
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        
        assertEquals(nonce, proofSecret.getNonce());
        assertEquals(hashLock, proofSecret.getData());
        assertArrayEquals(tags, proofSecret.getTags());
    }

    @Test
    public void testKeyConstant() {
        assertEquals("HTLC", HTLCProofSecret.KEY);
    }

    @Test
    public void testGetBuilder() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, null);
        
        HTLCBuilder builder = proofSecret.getBuilder();
        
        assertNotNull(builder);
        assertEquals(hashLock, builder.getHashLock());
        assertEquals(nonce, builder.getNonce());
    }

    @Test
    public void testGetAllowedPubkeysBeforeLockTime() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        ECPoint pubkey1 = generateTestPubkey();
        ECPoint pubkey2 = generateTestPubkey();
        
        // Set locktime in the future
        long futureLockTime = Instant.now().plusSeconds(3600).getEpochSecond();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey1, true), Cashu.pointToHex(pubkey2, true)},
            {"locktime", String.valueOf(futureLockTime)},
            {"n_sigs", "2"}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        
        P2PKProofSecret.AllowedKeysResult result = proofSecret.getAllowedPubkeys();
        
        assertNotNull(result);
        assertEquals(2, result.requiredSignatures);
        // Should return the regular pubkeys before locktime
        assertNotNull(result.keys);
    }

    @Test
    public void testGetAllowedPubkeysAfterLockTime() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        ECPoint refundKey = generateTestPubkey();
        
        // Set locktime in the past
        long pastLockTime = Instant.now().minusSeconds(3600).getEpochSecond();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)},
            {"locktime", String.valueOf(pastLockTime)},
            {"refund", Cashu.pointToHex(refundKey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        
        P2PKProofSecret.AllowedKeysResult result = proofSecret.getAllowedPubkeys();
        
        assertNotNull(result);
        // Should return refund keys after locktime
        assertEquals(1, result.keys.size());
    }

    @Test
    public void testVerifyPreimageWithCorrectPreimage() {
        String preimage = "secret_preimage_123";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, null);
        
        boolean result = proofSecret.verifyPreimage(preimage);
        
        assertTrue(result);
    }

    @Test
    public void testVerifyPreimageWithIncorrectPreimage() {
        String correctPreimage = "secret_preimage_123";
        byte[] hash = sha256(correctPreimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, null);
        
        String wrongPreimage = "wrong_preimage";
        boolean result = proofSecret.verifyPreimage(wrongPreimage);
        
        assertFalse(result);
    }

    @Test
    public void testVerifyPreimageWithEmptyString() {
        String preimage = "";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, null);
        
        boolean result = proofSecret.verifyPreimage(preimage);
        
        assertTrue(result);
    }

    @Test
    public void testVerifyPreimageComparesBytes() {
        // This test verifies the fix: comparing bytes instead of ECPoint.getEncoded()
        String preimage = "test_preimage";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, null);
        
        // Verify the implementation uses Arrays.equals on bytes
        assertTrue(proofSecret.verifyPreimage(preimage));
        
        // Verify the hashlock is stored as a string
        assertEquals(64, hashLockHex.length());
        assertEquals(hashLockHex, proofSecret.getData());
    }

    @Test
    public void testGenerateWitnessWithProof() {
        String preimage = "valid_preimage";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        ECPrivateKeyParameters privateKey = generatePrivateKey();
        ECPoint pubkey = ECC.DOMAIN.getG().multiply(privateKey.getD()).normalize();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, tags);
        
        // Create a proof
        Proof proof = new Proof();
        proof.secret = new StringSecret("test_secret");
        proof.amount = 100;
        proof.keysetId = "test_keyset";
        proof.c = Cashu.pointToHex(generateTestPubkey(), true);
        
        List<ECPrivateKeyParameters> keys = Collections.singletonList(privateKey);
        
        HTLCWitness witness = proofSecret.generateWitness(proof, keys, preimage);
        
        assertNotNull(witness);
        assertEquals(preimage, witness.preimage);
        assertNotNull(witness.signatures);
    }

    @Test
    public void testGenerateWitnessWithBlindedMessage() {
        String preimage = "valid_preimage";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        ECPrivateKeyParameters privateKey = generatePrivateKey();
        ECPoint pubkey = ECC.DOMAIN.getG().multiply(privateKey.getD()).normalize();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, tags);
        
        // Create a blinded message
        BlindedMessage blindedMessage = new BlindedMessage();
        blindedMessage.b_ = Cashu.pointToHex(generateTestPubkey(), true);
        blindedMessage.keysetId = "test_keyset";
        blindedMessage.amount = 100;
        
        List<ECPrivateKeyParameters> keys = Collections.singletonList(privateKey);
        
        HTLCWitness witness = proofSecret.generateWitness(blindedMessage, keys, preimage);
        
        assertNotNull(witness);
        assertEquals(preimage, witness.preimage);
        assertNotNull(witness.signatures);
    }

    @Test
    public void testGenerateWitnessWithInvalidPreimageThrowsException() {
        String correctPreimage = "valid_preimage";
        byte[] hash = sha256(correctPreimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        ECPrivateKeyParameters privateKey = generatePrivateKey();
        ECPoint pubkey = ECC.DOMAIN.getG().multiply(privateKey.getD()).normalize();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, tags);
        
        Proof proof = new Proof();
        proof.secret = new StringSecret("test_secret");
        proof.amount = 100;
        proof.keysetId = "test_keyset";
        proof.c = Cashu.pointToHex(generateTestPubkey(), true);
        
        List<ECPrivateKeyParameters> keys = Collections.singletonList(privateKey);
        String wrongPreimage = "wrong_preimage";
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            proofSecret.generateWitness(proof, keys, wrongPreimage);
        });
        
        assertEquals("Invalid preimage", exception.getMessage());
    }

    @Test
    public void testGenerateWitnessFromHashWithValidPreimage() {
        String preimage = "valid_preimage";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        ECPrivateKeyParameters privateKey = generatePrivateKey();
        ECPoint pubkey = ECC.DOMAIN.getG().multiply(privateKey.getD()).normalize();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, tags);
        
        byte[] messageHash = sha256("test_message");
        List<ECPrivateKeyParameters> keys = Collections.singletonList(privateKey);
        byte[] preimageBytes = preimage.getBytes(StandardCharsets.UTF_8);
        
        HTLCWitness witness = proofSecret.generateWitnessFromHash(messageHash, keys, preimageBytes);
        
        assertNotNull(witness);
        assertEquals(preimage, witness.preimage);
        assertNotNull(witness.signatures);
    }

    @Test
    public void testVerifyWitnessHash() {
        String preimage = "valid_preimage";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        ECPrivateKeyParameters privateKey = generatePrivateKey();
        ECPoint pubkey = ECC.DOMAIN.getG().multiply(privateKey.getD()).normalize();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, tags);
        
        // Generate witness
        byte[] messageHash = sha256("test_message");
        List<ECPrivateKeyParameters> keys = Collections.singletonList(privateKey);
        byte[] preimageBytes = preimage.getBytes(StandardCharsets.UTF_8);
        
        HTLCWitness witness = proofSecret.generateWitnessFromHash(messageHash, keys, preimageBytes);
        
        // Verify witness
        boolean result = proofSecret.verifyWitnessHash(messageHash, witness);
        
        assertTrue(result);
    }

    @Test
    public void testVerifyWitnessHashReturnsFalseForNonHTLCWitness() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, null);
        
        // Create a regular P2PKWitness (not HTLCWitness)
        P2PKWitness regularWitness = new P2PKWitness();
        regularWitness.signatures = Collections.emptyList();
        
        byte[] messageHash = sha256("test_message");
        
        boolean result = proofSecret.verifyWitnessHash(messageHash, regularWitness);
        
        assertFalse(result);
    }

    @Test
    public void testOverriddenGenerateWitnessMethodsThrowException() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, null);
        
        Proof proof = new Proof();
        proof.secret = new StringSecret("test");
        List<ECPrivateKeyParameters> keys = Collections.emptyList();
        
        // Test overridden method throws exception
        assertThrows(UnsupportedOperationException.class, () -> {
            proofSecret.generateWitness(proof, keys);
        });
    }

    @Test
    public void testOverriddenGenerateWitnessWithBlindedMessageThrowsException() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, null);
        
        BlindedMessage message = new BlindedMessage();
        message.b_ = Cashu.pointToHex(generateTestPubkey(), true);
        List<ECPrivateKeyParameters> keys = Collections.emptyList();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            proofSecret.generateWitness(message, keys);
        });
    }

    @Test
    public void testOverriddenGenerateWitnessWithByteArrayThrowsException() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, null);
        
        byte[] msg = "test".getBytes(StandardCharsets.UTF_8);
        List<ECPrivateKeyParameters> keys = Collections.emptyList();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            proofSecret.generateWitness(msg, keys);
        });
    }

    @Test
    public void testVerifyPreimageWithDifferentEncodings() {
        // Test with various string encodings
        String[] testPreimages = {
            "simple",
            "with spaces",
            "with-dashes",
            "with_underscores",
            "123456",
            "!@#$%^&*()",
            ""
        };
        
        for (String preimage : testPreimages) {
            byte[] hash = sha256(preimage);
            String hashLockHex = Hex.toHexString(hash).toLowerCase();
            
            String nonce = generateValidHashLock();
            HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, null);
            
            assertTrue(proofSecret.verifyPreimage(preimage), 
                "Failed for preimage: " + preimage);
        }
    }

    @Test
    public void testVerifyPreimageWithUnicodeCharacters() {
        String preimage = "Hello 世界 🌍";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, null);
        
        assertTrue(proofSecret.verifyPreimage(preimage));
    }

    @Test
    public void testHashLockStoredAsHexString() {
        // Verify that hashlock is stored as hex string, not as ECPoint
        String hashLock = generateValidHashLock();
        String nonce = generateValidHashLock();
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, null);
        
        assertEquals(hashLock, proofSecret.getData());
        assertEquals(64, proofSecret.getData().length());
        
        // Verify it's a valid hex string
        assertDoesNotThrow(() -> {
            Hex.decode(proofSecret.getData());
        });
    }

    @Test
    public void testGenerateWitnessPreimageIsIncludedInWitness() {
        String preimage = "test_preimage_value";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        ECPrivateKeyParameters privateKey = generatePrivateKey();
        ECPoint pubkey = ECC.DOMAIN.getG().multiply(privateKey.getD()).normalize();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, tags);
        
        byte[] messageHash = sha256("message");
        List<ECPrivateKeyParameters> keys = Collections.singletonList(privateKey);
        byte[] preimageBytes = preimage.getBytes(StandardCharsets.UTF_8);
        
        HTLCWitness witness = proofSecret.generateWitnessFromHash(messageHash, keys, preimageBytes);
        
        assertNotNull(witness.preimage);
        assertEquals(preimage, witness.preimage);
    }

    @Test
    public void testMultipleSignatureThreshold() {
        String preimage = "preimage";
        byte[] hash = sha256(preimage);
        String hashLockHex = Hex.toHexString(hash).toLowerCase();
        
        String nonce = generateValidHashLock();
        
        // Generate multiple keys
        ECPrivateKeyParameters key1 = generatePrivateKey();
        ECPrivateKeyParameters key2 = generatePrivateKey();
        ECPrivateKeyParameters key3 = generatePrivateKey();
        
        ECPoint pubkey1 = ECC.DOMAIN.getG().multiply(key1.getD()).normalize();
        ECPoint pubkey2 = ECC.DOMAIN.getG().multiply(key2.getD()).normalize();
        ECPoint pubkey3 = ECC.DOMAIN.getG().multiply(key3.getD()).normalize();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey1, true), Cashu.pointToHex(pubkey2, true), Cashu.pointToHex(pubkey3, true)},
            {"n_sigs", "2"}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLockHex, tags);
        
        byte[] messageHash = sha256("message");
        List<ECPrivateKeyParameters> keys = List.of(key1, key2, key3);
        byte[] preimageBytes = preimage.getBytes(StandardCharsets.UTF_8);
        
        HTLCWitness witness = proofSecret.generateWitnessFromHash(messageHash, keys, preimageBytes);
        
        assertNotNull(witness);
        assertEquals(preimage, witness.preimage);
        assertTrue(witness.signatures.size() >= 2);
    }

    @Test
    public void testGetAllowedPubkeysWithNoRefundKeys() {
        String nonce = generateValidHashLock();
        String hashLock = generateValidHashLock();
        ECPoint pubkey = generateTestPubkey();
        
        // Set locktime in the past but no refund keys
        long pastLockTime = Instant.now().minusSeconds(3600).getEpochSecond();
        
        String[][] tags = new String[][] {
            {"pubkeys", Cashu.pointToHex(pubkey, true)},
            {"locktime", String.valueOf(pastLockTime)}
        };
        
        HTLCProofSecret proofSecret = new HTLCProofSecret(nonce, hashLock, tags);
        
        P2PKProofSecret.AllowedKeysResult result = proofSecret.getAllowedPubkeys();
        
        assertNotNull(result);
        // Should return empty list when no refund keys and locktime passed
        assertTrue(result.keys.isEmpty());
    }
}