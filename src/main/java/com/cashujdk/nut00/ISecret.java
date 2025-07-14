package com.cashujdk.nut00;

import com.cashujdk.serialization.SecretDeserializer;
import com.cashujdk.serialization.SecretSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.bouncycastle.math.ec.ECPoint;

//Interface that all secrets (both nut 00 and nut10) has to implement
@JsonSerialize(using = SecretSerializer.class)
@JsonDeserialize(using = SecretDeserializer.class)
public interface ISecret {
    byte[] getBytes();
    ECPoint toCurve();
}
