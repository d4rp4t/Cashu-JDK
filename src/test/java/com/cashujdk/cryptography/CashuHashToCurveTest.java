package com.cashujdk.cryptography;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CashuHashToCurveTest {

    @Test
    public void testHashToCurveFromVector1() {
        // Test vector 1
        String message = "0000000000000000000000000000000000000000000000000000000000000000";
        String expectedPoint = "024cce997d3b518f739663b757deaec95bcd9473c30a14ac2fd04023a739d1a725";

        byte[] messageBytes = Hex.decode(message);
        String resultPoint = Cashu.pointToHex(Cashu.hashToCurve(messageBytes), true);

        assertEquals(expectedPoint, resultPoint);
    }

    @Test
    public void testHashToCurveFromVector2() {
        // Test vector 2
        String message = "0000000000000000000000000000000000000000000000000000000000000001";
        String expectedPoint = "022e7158e11c9506f1aa4248bf531298daa7febd6194f003edcd9b93ade6253acf";

        byte[] messageBytes = Hex.decode(message);
        String resultPoint = Cashu.pointToHex(Cashu.hashToCurve(messageBytes), true);

        assertEquals(expectedPoint, resultPoint);
    }

    @Test
    public void testHashToCurveFromVector3() {
        // Test vector 3 - takes a few iterations before finding valid point
        String message = "0000000000000000000000000000000000000000000000000000000000000002";
        String expectedPoint = "026cdbe15362df59cd1dd3c9c11de8aedac2106eca69236ecd9fbe117af897be4f";

        byte[] messageBytes = Hex.decode(message);
        String resultPoint = Cashu.pointToHex(Cashu.hashToCurve(messageBytes), true);

        assertEquals(expectedPoint, resultPoint);
    }
}
