package com.cashujdk;

import com.cashujdk.nut00.Proof;
import com.cashujdk.nut00.StringSecret;
import com.cashujdk.nut03.PostSwapRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.bouncycastle.util.encoders.Hex;

import java.util.Arrays;

public class SecretEncodingTest {
    
    @Test
    public void testHexSecretEncoding() throws Exception {
        // Test with a hex string secret
        String originalHex = "e773abcc60c50ab79d9d4d8951b9fefe";
        StringSecret secret = new StringSecret(originalHex);
        
        // Verify the secret is preserved
        assertEquals(originalHex, secret.getSecret());
        
        // Create a proof and verify serialization
        Proof proof = new Proof(16, "004f7adf2a04356c", secret,
            "026d5fe16c073fc1163e9182ade828923c21b0a4ed0ad69562fd7e3a4a7a117dd5",
            java.util.Optional.empty(), java.util.Optional.empty());
        
        PostSwapRequest request = new PostSwapRequest();
        request.inputs = Arrays.asList(proof);
        request.outputs = Arrays.asList();
        
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);
        System.out.println("Hex secret JSON:");
        System.out.println(json);
        
        // Verify the secret in JSON matches original
        assertTrue(json.contains("\"secret\":\"" + originalHex + "\""));
    }
    
    @Test
    public void testNonHexSecretHandling() throws Exception {
        // Test with a non-hex string
        String originalString = "test123";
        StringSecret secret = new StringSecret(originalString);
        
        // Verify the secret is preserved
        assertEquals(originalString, secret.getSecret());
        
        // Verify the bytes are handled correctly
        byte[] bytes = secret.getBytes();
        assertArrayEquals(originalString.getBytes(), bytes);
    }
    
    @Test
    public void testRandomSecretGeneration() throws Exception {
        StringSecret secret = StringSecret.random();
        String hexString = secret.getSecret();
        
        // Verify it's a valid hex string
        assertNotNull(Hex.decode(hexString));
        assertEquals(64, hexString.length()); // 32 bytes = 64 hex chars
    }
}
