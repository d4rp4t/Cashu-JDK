package com.cashujdk.nut02;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class KeysetIdUtilTest {

    private static final Map<BigInteger, String> SMALL_KEYSET = createSmallKeyset();
    private static final Map<BigInteger, String> LARGE_KEYSET = createLargeKeyset();

    private static Map<BigInteger, String> createSmallKeyset() {
        Map<BigInteger, String> keys = new LinkedHashMap<>();
        keys.put(BigInteger.valueOf(1), "03a40f20667ed53513075dc51e715ff2046cad64eb68960632269ba7f0210e38bc");
        keys.put(BigInteger.valueOf(2), "03fd4ce5a16b65576145949e6f99f445f8249fee17c606b688b504a849cdc452de");
        keys.put(BigInteger.valueOf(4), "02648eccfa4c026960966276fa5a4cae46ce0fd432211a4f449bf84f13aa5f8303");
        keys.put(BigInteger.valueOf(8), "02fdfd6796bfeac490cbee12f778f867f0a2c68f6508d17c649759ea0dc3547528");
        return keys;
    }

    private static Map<BigInteger, String> createLargeKeyset() {
        Map<BigInteger, String> keys = new LinkedHashMap<>();
        // Adding all 64 keys
        keys.put(BigInteger.valueOf(1), "03ba786a2c0745f8c30e490288acd7a72dd53d65afd292ddefa326a4a3fa14c566");
        keys.put(BigInteger.valueOf(2), "03361cd8bd1329fea797a6add1cf1990ffcf2270ceb9fc81eeee0e8e9c1bd0cdf5");
        keys.put(BigInteger.valueOf(4), "036e378bcf78738ddf68859293c69778035740e41138ab183c94f8fee7572214c7");
        keys.put(BigInteger.valueOf(8), "03909d73beaf28edfb283dbeb8da321afd40651e8902fcf5454ecc7d69788626c0");
        keys.put(BigInteger.valueOf(16), "028a36f0e6638ea7466665fe174d958212723019ec08f9ce6898d897f88e68aa5d");
        keys.put(BigInteger.valueOf(32), "03a97a40e146adee2687ac60c2ba2586a90f970de92a9d0e6cae5a4b9965f54612");
        keys.put(BigInteger.valueOf(64), "03ce86f0c197aab181ddba0cfc5c5576e11dfd5164d9f3d4a3fc3ffbbf2e069664");
        keys.put(BigInteger.valueOf(128), "0284f2c06d938a6f78794814c687560a0aabab19fe5e6f30ede38e113b132a3cb9");
        keys.put(BigInteger.valueOf(256), "03b99f475b68e5b4c0ba809cdecaae64eade2d9787aa123206f91cd61f76c01459");
        keys.put(BigInteger.valueOf(512), "03d4db82ea19a44d35274de51f78af0a710925fe7d9e03620b84e3e9976e3ac2eb");
        keys.put(BigInteger.valueOf(1024), "031fbd4ba801870871d46cf62228a1b748905ebc07d3b210daf48de229e683f2dc");
        keys.put(BigInteger.valueOf(2048), "0276cedb9a3b160db6a158ad4e468d2437f021293204b3cd4bf6247970d8aff54b");
        keys.put(BigInteger.valueOf(4096), "02fc6b89b403ee9eb8a7ed457cd3973638080d6e04ca8af7307c965c166b555ea2");
        keys.put(BigInteger.valueOf(8192), "0320265583e916d3a305f0d2687fcf2cd4e3cd03a16ea8261fda309c3ec5721e21");
        keys.put(BigInteger.valueOf(16384), "036e41de58fdff3cb1d8d713f48c63bc61fa3b3e1631495a444d178363c0d2ed50");
        keys.put(BigInteger.valueOf(32768), "0365438f613f19696264300b069d1dad93f0c60a37536b72a8ab7c7366a5ee6c04");
        keys.put(BigInteger.valueOf(65536), "02408426cfb6fc86341bac79624ba8708a4376b2d92debdf4134813f866eb57a8d");
        keys.put(BigInteger.valueOf(131072), "031063e9f11c94dc778c473e968966eac0e70b7145213fbaff5f7a007e71c65f41");
        keys.put(BigInteger.valueOf(262144), "02f2a3e808f9cd168ec71b7f328258d0c1dda250659c1aced14c7f5cf05aab4328");
        keys.put(BigInteger.valueOf(524288), "038ac10de9f1ff9395903bb73077e94dbf91e9ef98fd77d9a2debc5f74c575bc86");
        keys.put(BigInteger.valueOf(1048576), "0203eaee4db749b0fc7c49870d082024b2c31d889f9bc3b32473d4f1dfa3625788");
        keys.put(BigInteger.valueOf(2097152), "033cdb9d36e1e82ae652b7b6a08e0204569ec7ff9ebf85d80a02786dc7fe00b04c");
        keys.put(BigInteger.valueOf(4194304), "02c8b73f4e3a470ae05e5f2fe39984d41e9f6ae7be9f3b09c9ac31292e403ac512");
        keys.put(BigInteger.valueOf(8388608), "025bbe0cfce8a1f4fbd7f3a0d4a09cb6badd73ef61829dc827aa8a98c270bc25b0");
        keys.put(BigInteger.valueOf(16777216), "037eec3d1651a30a90182d9287a5c51386fe35d4a96839cf7969c6e2a03db1fc21");
        keys.put(BigInteger.valueOf(33554432), "03280576b81a04e6abd7197f305506476f5751356b7643988495ca5c3e14e5c262");
        keys.put(BigInteger.valueOf(67108864), "03268bfb05be1dbb33ab6e7e00e438373ca2c9b9abc018fdb452d0e1a0935e10d3");
        keys.put(BigInteger.valueOf(134217728), "02573b68784ceba9617bbcc7c9487836d296aa7c628c3199173a841e7a19798020");
        keys.put(BigInteger.valueOf(268435456), "0234076b6e70f7fbf755d2227ecc8d8169d662518ee3a1401f729e2a12ccb2b276");
        keys.put(BigInteger.valueOf(536870912), "03015bd88961e2a466a2163bd4248d1d2b42c7c58a157e594785e7eb34d880efc9");
        keys.put(BigInteger.valueOf(1073741824), "02c9b076d08f9020ebee49ac8ba2610b404d4e553a4f800150ceb539e9421aaeee");
        keys.put(BigInteger.valueOf(2147483648L), "034d592f4c366afddc919a509600af81b489a03caf4f7517c2b3f4f2b558f9a41a");
        keys.put(BigInteger.valueOf(4294967296L), "037c09ecb66da082981e4cbdb1ac65c0eb631fc75d85bed13efb2c6364148879b5");
        keys.put(BigInteger.valueOf(8589934592L), "02b4ebb0dda3b9ad83b39e2e31024b777cc0ac205a96b9a6cfab3edea2912ed1b3");
        keys.put(BigInteger.valueOf(17179869184L), "026cc4dacdced45e63f6e4f62edbc5779ccd802e7fabb82d5123db879b636176e9");
        keys.put(BigInteger.valueOf(34359738368L), "02b2cee01b7d8e90180254459b8f09bbea9aad34c3a2fd98c85517ecfc9805af75");
        keys.put(BigInteger.valueOf(68719476736L), "037a0c0d564540fc574b8bfa0253cca987b75466e44b295ed59f6f8bd41aace754");
        keys.put(BigInteger.valueOf(137438953472L), "021df6585cae9b9ca431318a713fd73dbb76b3ef5667957e8633bca8aaa7214fb6");
        keys.put(BigInteger.valueOf(274877906944L), "02b8f53dde126f8c85fa5bb6061c0be5aca90984ce9b902966941caf963648d53a");
        keys.put(BigInteger.valueOf(549755813888L), "029cc8af2840d59f1d8761779b2496623c82c64be8e15f9ab577c657c6dd453785");
        keys.put(BigInteger.valueOf(1099511627776L), "03e446fdb84fad492ff3a25fc1046fb9a93a5b262ebcd0151caa442ea28959a38a");
        keys.put(BigInteger.valueOf(2199023255552L), "02d6b25bd4ab599dd0818c55f75702fde603c93f259222001246569018842d3258");
        keys.put(BigInteger.valueOf(4398046511104L), "03397b522bb4e156ec3952d3f048e5a986c20a00718e5e52cd5718466bf494156a");
        keys.put(BigInteger.valueOf(8796093022208L), "02d1fb9e78262b5d7d74028073075b80bb5ab281edcfc3191061962c1346340f1e");
        keys.put(BigInteger.valueOf(17592186044416L), "030d3f2ad7a4ca115712ff7f140434f802b19a4c9b2dd1c76f3e8e80c05c6a9310");
        keys.put(BigInteger.valueOf(35184372088832L), "03e325b691f292e1dfb151c3fb7cad440b225795583c32e24e10635a80e4221c06");
        keys.put(BigInteger.valueOf(70368744177664L), "03bee8f64d88de3dee21d61f89efa32933da51152ddbd67466bef815e9f93f8fd1");
        keys.put(BigInteger.valueOf(140737488355328L), "0327244c9019a4892e1f04ba3bf95fe43b327479e2d57c25979446cc508cd379ed");
        keys.put(BigInteger.valueOf(281474976710656L), "02fb58522cd662f2f8b042f8161caae6e45de98283f74d4e99f19b0ea85e08a56d");
        keys.put(BigInteger.valueOf(562949953421312L), "02adde4b466a9d7e59386b6a701a39717c53f30c4810613c1b55e6b6da43b7bc9a");
        keys.put(BigInteger.valueOf(1125899906842624L), "038eeda11f78ce05c774f30e393cda075192b890d68590813ff46362548528dca9");
        keys.put(BigInteger.valueOf(2251799813685248L), "02ec13e0058b196db80f7079d329333b330dc30c000dbdd7397cbbc5a37a664c4f");
        keys.put(BigInteger.valueOf(4503599627370496L), "02d2d162db63675bd04f7d56df04508840f41e2ad87312a3c93041b494efe80a73");
        keys.put(BigInteger.valueOf(9007199254740992L), "0356969d6aef2bb40121dbd07c68b6102339f4ea8e674a9008bb69506795998f49");
        keys.put(BigInteger.valueOf(18014398509481984L), "02f4e667567ebb9f4e6e180a4113bb071c48855f657766bb5e9c776a880335d1d6");
        keys.put(BigInteger.valueOf(36028797018963968L), "0385b4fe35e41703d7a657d957c67bb536629de57b7e6ee6fe2130728ef0fc90b0");
        keys.put(BigInteger.valueOf(72057594037927936L), "02b2bc1968a6fddbcc78fb9903940524824b5f5bed329c6ad48a19b56068c144fd");
        keys.put(BigInteger.valueOf(144115188075855872L), "02e0dbb24f1d288a693e8a49bc14264d1276be16972131520cf9e055ae92fba19a");
        keys.put(BigInteger.valueOf(288230376151711744L), "03efe75c106f931a525dc2d653ebedddc413a2c7d8cb9da410893ae7d2fa7d19cc");
        keys.put(BigInteger.valueOf(576460752303423488L), "02c7ec2bd9508a7fc03f73c7565dc600b30fd86f3d305f8f139c45c404a52d958a");
        keys.put(BigInteger.valueOf(1152921504606846976L), "035a6679c6b25e68ff4e29d1c7ef87f21e0a8fc574f6a08c1aa45ff352c1d59f06");
        keys.put(BigInteger.valueOf(2305843009213693952L), "033cdc225962c052d485f7cfbf55a5b2367d200fe1fe4373a347deb4cc99e9a099");
        keys.put(BigInteger.valueOf(4611686018427387904L), "024a4b806cf413d14b294719090a9da36ba75209c7657135ad09bc65328fba9e6f");
        keys.put(BigInteger.valueOf(9223372036854775807L), "0377a6fe114e291a8d8e991627c38001c8305b23b9e98b1c7b1893f5cd0dda6cad");
        return keys;
    }

    @Test
    public void testVersion1SmallKeyset() {
        // Version 1 test - from first example in test vectors
        String id = KeysetIdUtil.getId((byte)0x00, SMALL_KEYSET, null, Optional.empty());
        assertEquals("00456a94ab4e1c46", id);
    }

    @Test
    public void testVersion1LargeKeyset() {
        // Version 1 test - from second example in test vectors
        String id = KeysetIdUtil.getId((byte)0x00, LARGE_KEYSET, null, Optional.empty());
        assertEquals("000f01df73ea149a", id);
    }
    
    @Test
    public void testVersion2SmallKeysetWithExpiry() {
        // Version 2 test with expiry - first vector
        String id = KeysetIdUtil.getId((byte)0x01, SMALL_KEYSET, "sat", Optional.of(2059210353L));
        assertEquals("01adc013fa9d85171586660abab27579888611659d357bc86bc09cb26eee8bc035", id);
    }

    @Test
    public void testVersion2LargeKeysetWithExpiry() {
        // Version 2 test with expiry - second vector
        String id = KeysetIdUtil.getId((byte)0x01, LARGE_KEYSET, "sat", Optional.of(2059210353L));
        assertEquals("0125bc634e270ad7e937af5b957f8396bb627d73f6e1fd2ffe4294c26b57daf9e0", id);
    }

    @Test
    public void testVersion2LargeKeysetNoExpiry() {
        // Version 2 test without expiry - third vector
        String id = KeysetIdUtil.getId((byte)0x01, LARGE_KEYSET, "sat", Optional.empty());
        assertEquals("016d72f27c8d22808ad66d1959b3dab83af17e2510db7ffd57d2365d9eec3ced75", id);
    }

    @Test
    public void testInvalidVersion() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            KeysetIdUtil.getId((byte)0x02, SMALL_KEYSET, "sat", Optional.empty());
        });
        
        assertTrue(exception.getMessage().contains("Unrecognized keyset version"));
    }
}
