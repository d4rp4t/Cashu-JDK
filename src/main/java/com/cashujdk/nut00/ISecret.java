package com.cashujdk.nut00;

import org.bouncycastle.math.ec.ECPoint;

//Interface that all secrets (both nut 00 and nut10) has to implement
public interface ISecret {
    byte[] getBytes();
    ECPoint toCurve();
}
