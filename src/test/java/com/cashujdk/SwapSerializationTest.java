package com.cashujdk;

import com.cashujdk.nut00.Proof;
import com.cashujdk.nut00.StringSecret;
import com.cashujdk.nut03.PostSwapRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class SwapSerializationTest {
    
    @Test
    public void testSwapRequestSerialization() throws Exception {
        // Create a test proof
        String hexSecret = "c4dede17ef281016cab0f4075509c615c68f8f772312bb375e2096561ef402a6";
        Proof proof = new Proof(64, "00500550f0494146", new StringSecret(hexSecret), 
            "027720576baa4c11717df3a74af353b8b49646e3ce528f145d58a18788376ca972", 
            java.util.Optional.empty(), java.util.Optional.empty());
        
        // Create a swap request
        PostSwapRequest request = new PostSwapRequest();
        request.inputs = Arrays.asList(proof);
        request.outputs = Arrays.asList(); // Empty for test
        
        // Test default ObjectMapper (like in CashuHttpClient)
        ObjectMapper defaultMapper = new ObjectMapper();
        System.out.println("Default ObjectMapper serialization:");
        System.out.println(defaultMapper.writeValueAsString(request));
        
        // Test ObjectMapper with custom module
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule());
        System.out.println("\nCustom ObjectMapper serialization:");
        System.out.println(customMapper.writeValueAsString(request));
    }
}
