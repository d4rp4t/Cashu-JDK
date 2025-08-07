package com.cashujdk.nut00;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TokenDeserializationTest {
    
    @Test
    public void testTokenDeserialization() throws Exception {
        String tokenStr = "cashuBo2FteCJodHRwczovL21pbnQubWluaWJpdHMuY2FzaC9CaXRjb2luYXVjc2F0YXSBomFpSABQBVDwSUFGYXCDpGFhGEBhc3hAYzRkZWRlMTdlZjI4MTAxNmNhYjBmNDA3NTUwOWM2MTVjNjhmOGY3NzIzMTJiYjM3NWUyMDk2NTYxZWY0MDJhNmFjWCECdyBXa6pMEXF986dK81O4tJZG485SjxRdWKGHiDdsqXJhZKNhZVggVGd4MrCsuSSsRYhp5U7-62V_958Nb9fP8Y-r-nBRZLBhc1ggqOVTbu8cqzr1v-FRsAa9JtQJseFRsp3lOMQWBAxyP-Bhclgg886-5IZk8bQNzIXw9SsMfZLmS2s3aelOvamgNJtXpqSkYWEYIGFzeEBhOTJiZDk1OTA4ZTllN2Y2NjIxY2YwYzQ5NTFmYWEyMjdiNzlmNWUxNDlhODVlYTlkZGM4NTgwZGVhNzhjYjM3YWNYIQN4VRIix4OIuibkCe3n3-UySV5QJxbhtS4jca2Vs_79ymFko2FlWCBhQBGaBLkwcliNVGy14i1ZtZE30sm2_ROsRQGqDjyKBmFzWCCk0Q_w39MA4Sr3xDR9x3ak4BQLVdAS3HQ2_Jis-PvM_mFyWCD9ZER8Sh64eEyPFLUatc5ikf0MblRNOxhM2-8LxBg-cqRhYQFhc3hAMGVkOWRkZTQ3M2NjZDQ4Njk1Y2E4YjQ4OGE2ZWNiNjE5Yjg5NTI2MTc5N2Q4MzM1M2U4ZGI0Mjg2NWJlOTg5NWFjWCEDbOpp8SSvjandJEbVxi8gNwP9RsOwG0OZ2I6_zjaUWSBhZKNhZVgg5bX6zxgNXynh-wFk4NiEV0zJ2hVoeROXAf5v_VLUMaJhc1ggyczr8nxZcN9yq_kmooCjcCwvKpPk4JxYNDhbS1w4YhJhclggzY_D6zDOsuv_1Hj0IVnS363bZOsPrjDJtvFvxMf8rTw";
        
        // Decode the Token
        Token token = Token.decode(tokenStr);
        
        // Print token details
        System.out.println("Decoded Token:");
        System.out.println("Mint URL: " + token.mint);
        System.out.println("Unit: " + token.unit);
        System.out.println("Memo: " + token.memo);
        
        // Print tokens
        System.out.println("\nInner Tokens:");
        if (token.tokens != null) {
            token.tokens.forEach(innerToken -> {
                System.out.println("Inner Token:");
                if (innerToken.proofs != null) {
                    innerToken.proofs.forEach(proof -> {
                        System.out.println("  Amount: " + proof.amount);
                        if (proof.secret instanceof StringSecret) {
                            System.out.println("  Secret (hex): " + ((StringSecret)proof.secret).getSecret());
                            System.out.println("  Secret (bytes): " + bytesToHex(((StringSecret)proof.secret).getBytes()));
                        } else {
                            System.out.println("  Secret (type): " + proof.secret.getClass().getName());
                        }
                        System.out.println("  C: " + proof.c);
                        if (proof.keysetId != null) {
                            System.out.println("  Keyset ID: " + proof.keysetId);
                        }
                        System.out.println("  ---");
                    });
                }
                System.out.println("================");
            });
        }
        
        // Verify the token was deserialized correctly
        assertNotNull(token);
        assertNotNull(token.tokens);
        assertFalse(token.tokens.isEmpty());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
