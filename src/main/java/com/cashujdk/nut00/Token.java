package com.cashujdk.nut00;

import com.cashujdk.serialization.CBORSerializer;
import com.cashujdk.serialization.CBORDeserializer;

import java.util.Base64;
import java.util.List;

public class Token {
    public String mint;
    public String unit;
    public String memo;
    public List<InnerToken> tokens;


    public String encode() throws Exception {
        var serializer = new CBORSerializer();
        byte[] cbor = serializer.toCBOR(this);
        String base64 = Base64.getUrlEncoder().encodeToString(cbor);
        return "cashuB" + base64;
    }


    public static Token decode(String encoded) throws Exception {
        if (!encoded.startsWith("cashuB")) throw new IllegalArgumentException("invalid token");
        String base64 = encoded.substring(6);
        byte[] cbor = Base64.getUrlDecoder().decode(base64);
        var deserializer = new CBORDeserializer();
        return deserializer.fromCBOR(cbor);
    }


//    public byte[] encodeCrawB() throws Exception {
//        CBORFactory cborFactory = new CBORFactory();
//        ObjectMapper mapper = new ObjectMapper(cborFactory);
//        byte[] cbor = mapper.writeValueAsBytes(this);
//        byte[] prefix = "crawB".getBytes();
//        byte[] result = new byte[prefix.length + cbor.length];
//        System.arraycopy(prefix, 0, result, 0, prefix.length);
//        System.arraycopy(cbor, 0, result, prefix.length, cbor.length);
//        return result;
//    }
//
//    public static Token decodeCrawB(byte[] data) throws Exception {
//        byte[] prefix = "crawB".getBytes();
//        for (int i = 0; i < prefix.length; i++) {
//            if (data[i] != prefix[i]) throw new IllegalArgumentException("invalid binary token");
//        }
//        byte[] cbor = new byte[data.length - prefix.length];
//        System.arraycopy(data, prefix.length, cbor, 0, cbor.length);
//        CBORFactory cborFactory = new CBORFactory();
//        ObjectMapper mapper = new ObjectMapper(cborFactory);
//        return mapper.readValue(cbor, Token.class);
//    }
}
