package com.project.cryptography;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.crypto.params.ECDomainParameters;

public class ECC {
    public static final ECNamedCurveParameterSpec CURVE_SPEC =
            ECNamedCurveTable.getParameterSpec("secp256k1");
    public static final ECDomainParameters DOMAIN =
            new ECDomainParameters(
                    CURVE_SPEC.getCurve(),
                    CURVE_SPEC.getG(),
                    CURVE_SPEC.getN(),
                    CURVE_SPEC.getH()
            );
}
