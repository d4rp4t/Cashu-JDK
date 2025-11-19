package com.cashujdk.cryptography;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import java.math.BigInteger;

public abstract class Secp256k1 {
    public static final SecP256K1Curve CURVE = new SecP256K1Curve();
    public static final ECPoint GENERATOR = ECC.DOMAIN.getG();
    public static final BigInteger CURVE_ORDER = CURVE.getOrder();
}