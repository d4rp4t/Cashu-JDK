package com.cashujdk;

import com.cashujdk.nut18.PaymentRequest;
import com.cashujdk.nut18.Transport;
import com.cashujdk.nut18.TransportTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

public class PaymentRequestTest {
    @Test
    public void serializesCorrectly() throws Exception {

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.id = Optional.of("b7a90176");
        paymentRequest.amount = Optional.of(10L);
        paymentRequest.unit = Optional.of("sat");
        paymentRequest.mints = Optional.of(new String[]{"https://nofees.testnut.cashu.space"});

        Transport transport = new Transport();
        TransportTag transportTag = new TransportTag();
        transportTag.key = "n";
        transportTag.value = "17";
        transport.tags = Optional.of(new TransportTag[] {transportTag});
        transport.type = "nostr";
        transport.target = "nprofile1qy28wumn8ghj7un9d3shjtnyv9kh2uewd9hsz9mhwden5te0wfjkccte9curxven9eehqctrv5hszrthwden5te0dehhxtnvdakqqgydaqy7curk439ykptkysv7udhdhu68sucm295akqefdehkf0d495cwunl5";

        paymentRequest.transport = Optional.of(new Transport[] {transport});

        String serialized = paymentRequest.encode();

        PaymentRequest testVector = PaymentRequest.decode("creqApWF0gaNhdGVub3N0cmFheKlucHJvZmlsZTFxeTI4d3VtbjhnaGo3dW45ZDNzaGp0bnl2OWtoMnVld2Q5aHN6OW1od2RlbjV0ZTB3ZmprY2N0ZTljdXJ4dmVuOWVlaHFjdHJ2NWhzenJ0aHdkZW41dGUwZGVoaHh0bnZkYWtxcWd5ZGFxeTdjdXJrNDM5eWtwdGt5c3Y3dWRoZGh1NjhzdWNtMjk1YWtxZWZkZWhrZjBkNDk1Y3d1bmw1YWeBgmFuYjE3YWloYjdhOTAxNzZhYQphdWNzYXRhbYF4Imh0dHBzOi8vbm9mZWVzLnRlc3RudXQuY2FzaHUuc3BhY2U=");
        PaymentRequest decoded = PaymentRequest.decode(serialized);

        assertEquals(testVector.id, decoded.id);
        assertEquals(testVector.amount, decoded.amount);
        assertEquals(testVector.unit, decoded.unit);
        assertEquals(testVector.description, decoded.description);
        assertEquals(testVector.description, decoded.description);
        assertArrayEquals(testVector.mints.get(), decoded.mints.get());

        Transport[] expected = testVector.transport.get();
        Transport[] actual = decoded.transport.get();

        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].type, actual[i].type);
            assertEquals(expected[i].target, actual[i].target);

            TransportTag[] tags1 = expected[i].tags.get();
            TransportTag[] tags2 = actual[i].tags.get();

            assertEquals(tags1.length, tags2.length);
            for (int j = 0; j < tags1.length; j++) {
                assertEquals(tags1[j].key, tags2[j].key);
                assertEquals(tags1[j].value, tags2[j].value);
            }
        }

        // Just to make sure, compare with hardcoded values
        assertEquals("b7a90176", testVector.id.get());
        assertEquals(10, testVector.amount.get());
        assertEquals("sat", testVector.unit.get());
        assertArrayEquals(new String[] {"https://nofees.testnut.cashu.space"}, testVector.mints.get());

        assertEquals("nostr", testVector.transport.get()[0].type);
        assertEquals("nprofile1qy28wumn8ghj7un9d3shjtnyv9kh2uewd9hsz9mhwden5te0wfjkccte9curxven9eehqctrv5hszrthwden5te0dehhxtnvdakqqgydaqy7curk439ykptkysv7udhdhu68sucm295akqefdehkf0d495cwunl5", testVector.transport.get()[0].target);
        assertEquals(testVector.transport.get()[0].tags.get()[0].key, "n");
        assertEquals(testVector.transport.get()[0].tags.get()[0].value, "17");

    }
}
