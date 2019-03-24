package org.bitcoin;

import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.bitcoin.NativeSecp256k1Util.*;

/**
 * This class holds test cases defined for testing this library.
 */
public class NativeSecp256k1Test {

    //TODO improve comments/add more tests
    /**
      * This tests verify() for a valid signature
      */
    @Test
    public void testVerifyPos() throws AssertFailException{
        byte[] data = DatatypeConverter.parseHexBinary("CF80CD8AED482D5D1527D7DC72FCEFF84E6326592848447D2DC0B0E87DFC9A90"); //sha256hash of "testing"
        byte[] sig = DatatypeConverter.parseHexBinary("3044022079BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F817980220294F14E883B3F525B5367756C2A11EF6CF84B730B36C17CB0C56F0AAB2C98589");
        byte[] pub = DatatypeConverter.parseHexBinary("040A629506E1B65CD9D2E0BA9C75DF9C4FED0DB16DC9625ED14397F0AFC836FAE595DC53F8B0EFE61E703075BD9B143BAC75EC0E19F82A2208CAEB32BE53414C40");

        boolean result = NativeSecp256k1.verify( data, sig, pub);
        assertEquals( result, true , "testVerifyPos");
    }

    /**
      * This tests verify() for a non-valid signature
      */
    @Test
    public void testVerifyNeg() throws AssertFailException{
        byte[] data = DatatypeConverter.parseHexBinary("CF80CD8AED482D5D1527D7DC72FCEFF84E6326592848447D2DC0B0E87DFC9A91"); //sha256hash of "testing"
        byte[] sig = DatatypeConverter.parseHexBinary("3044022079BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F817980220294F14E883B3F525B5367756C2A11EF6CF84B730B36C17CB0C56F0AAB2C98589");
        byte[] pub = DatatypeConverter.parseHexBinary("040A629506E1B65CD9D2E0BA9C75DF9C4FED0DB16DC9625ED14397F0AFC836FAE595DC53F8B0EFE61E703075BD9B143BAC75EC0E19F82A2208CAEB32BE53414C40");

        boolean result = NativeSecp256k1.verify( data, sig, pub);
        assertEquals( result, false , "testVerifyNeg");
    }

    /**
      * This tests secret key verify() for a valid secretkey
      */
    @Test
    public void testSecKeyVerifyPos() throws AssertFailException{
        byte[] sec = DatatypeConverter.parseHexBinary("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530");

        boolean result = NativeSecp256k1.secKeyVerify( sec );
        assertEquals( result, true , "testSecKeyVerifyPos");
    }

    /**
     * This tests secret key verify() for an invalid secretkey
     */
    public static void testSecKeyVerifyNeg() throws AssertFailException{
        byte[] sec = DatatypeConverter.parseHexBinary("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

        boolean result = NativeSecp256k1.secKeyVerify( sec );
        assertEquals( result, false , "testSecKeyVerifyNeg");
    }

    /**
      * This tests public key create() for a valid secretkey
      */
    @Test
    public void testPubKeyCreatePos() throws AssertFailException{
        byte[] sec = DatatypeConverter.parseHexBinary("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530");

        byte[] resultArr = NativeSecp256k1.computePubkey(sec, false);
        String pubkeyString = DatatypeConverter.printHexBinary(resultArr);
        assertEquals( pubkeyString , "04C591A8FF19AC9C4E4E5793673B83123437E975285E7B442F4EE2654DFFCA5E2D2103ED494718C697AC9AEBCFD19612E224DB46661011863ED2FC54E71861E2A6" , "testPubKeyCreatePos");
    }

    /**
      * This tests public key create() for a invalid secretkey
      */
    @Test
    public void testPubKeyCreateNeg() throws AssertFailException{
       byte[] sec = DatatypeConverter.parseHexBinary("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

       byte[] resultArr = NativeSecp256k1.computePubkey(sec, false);
       String pubkeyString = DatatypeConverter.printHexBinary(resultArr);
       assertEquals( pubkeyString, "" , "testPubKeyCreateNeg");
    }

    /**
      * This tests sign() for a valid secretkey
      */
    @Test
    public void testSignPos() throws AssertFailException{

        byte[] data = DatatypeConverter.parseHexBinary("CF80CD8AED482D5D1527D7DC72FCEFF84E6326592848447D2DC0B0E87DFC9A90"); //sha256hash of "testing"
        byte[] sec = DatatypeConverter.parseHexBinary("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530");

        byte[] resultArr = NativeSecp256k1.sign(data, sec);
        String sigString = DatatypeConverter.printHexBinary(resultArr);
        assertEquals( sigString, "3045022100F51D069AA46EDB4E2E77773FE364AA2AF6818AF733EA542CFC4D546640A58D8802204F1C442AC9F26F232451A0C3EE99F6875353FC73902C68055C19E31624F687CC" , "testSignPos");
    }

    /**
      * This tests sign() for a invalid secretkey
      */
    @Test
    public void testSignNeg() throws AssertFailException{
        byte[] data = DatatypeConverter.parseHexBinary("CF80CD8AED482D5D1527D7DC72FCEFF84E6326592848447D2DC0B0E87DFC9A90"); //sha256hash of "testing"
        byte[] sec = DatatypeConverter.parseHexBinary("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

        byte[] resultArr = NativeSecp256k1.sign(data, sec);
        String sigString = DatatypeConverter.printHexBinary(resultArr);
        assertEquals( sigString, "" , "testSignNeg");
    }

    /**
      * This tests private key tweak-add
      */
    @Test
    public void testPrivKeyTweakAdd() throws AssertFailException {
        byte[] sec = DatatypeConverter.parseHexBinary("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530");
        byte[] data = DatatypeConverter.parseHexBinary("3982F19BEF1615BCCFBB05E321C10E1D4CBA3DF0E841C2E41EEB6016347653C3"); //sha256hash of "tweak"

        byte[] resultArr = NativeSecp256k1.privKeyTweakAdd( sec , data );
        String seckeyString = DatatypeConverter.printHexBinary(resultArr);
        assertEquals( seckeyString , "A168571E189E6F9A7E2D657A4B53AE99B909F7E712D1C23CED28093CD57C88F3" , "testPrivKeyTweakAdd");
    }

    /**
      * This tests private key tweak-mul
      */
    @Test
    public void testPrivKeyTweakMul() throws AssertFailException {
        byte[] sec = DatatypeConverter.parseHexBinary("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530");
        byte[] data = DatatypeConverter.parseHexBinary("3982F19BEF1615BCCFBB05E321C10E1D4CBA3DF0E841C2E41EEB6016347653C3"); //sha256hash of "tweak"

        byte[] resultArr = NativeSecp256k1.privKeyTweakMul( sec , data );
        String seckeyString = DatatypeConverter.printHexBinary(resultArr);
        assertEquals( seckeyString , "97F8184235F101550F3C71C927507651BD3F1CDB4A5A33B8986ACF0DEE20FFFC" , "testPrivKeyTweakMul");
    }

    /**
      * This tests public key tweak-add
      */
    @Test
    public void testPubKeyTweakAdd() throws AssertFailException {
        byte[] pub = DatatypeConverter.parseHexBinary("040A629506E1B65CD9D2E0BA9C75DF9C4FED0DB16DC9625ED14397F0AFC836FAE595DC53F8B0EFE61E703075BD9B143BAC75EC0E19F82A2208CAEB32BE53414C40");
        byte[] data = DatatypeConverter.parseHexBinary("3982F19BEF1615BCCFBB05E321C10E1D4CBA3DF0E841C2E41EEB6016347653C3"); //sha256hash of "tweak"

        byte[] resultArr = NativeSecp256k1.pubKeyTweakAdd( pub , data, false);
        String pubkeyString = DatatypeConverter.printHexBinary(resultArr);
        byte[] resultArrCompressed = NativeSecp256k1.pubKeyTweakAdd( pub , data, true);
        String pubkeyStringCompressed = DatatypeConverter.printHexBinary(resultArrCompressed);
        assertEquals(pubkeyString , "0411C6790F4B663CCE607BAAE08C43557EDC1A4D11D88DFCB3D841D0C6A941AF525A268E2A863C148555C48FB5FBA368E88718A46E205FABC3DBA2CCFFAB0796EF" , "testPubKeyTweakAdd");
        assertEquals(pubkeyStringCompressed , "0311C6790F4B663CCE607BAAE08C43557EDC1A4D11D88DFCB3D841D0C6A941AF52" , "testPubKeyTweakAdd (compressed)");
    }

    /**
      * This tests public key tweak-mul
      */
    @Test
    public void testPubKeyTweakMul() throws AssertFailException {
        byte[] pub = DatatypeConverter.parseHexBinary("040A629506E1B65CD9D2E0BA9C75DF9C4FED0DB16DC9625ED14397F0AFC836FAE595DC53F8B0EFE61E703075BD9B143BAC75EC0E19F82A2208CAEB32BE53414C40");
        byte[] data = DatatypeConverter.parseHexBinary("3982F19BEF1615BCCFBB05E321C10E1D4CBA3DF0E841C2E41EEB6016347653C3"); //sha256hash of "tweak"

        byte[] resultArr = NativeSecp256k1.pubKeyTweakMul( pub , data, false);
        String pubkeyString = DatatypeConverter.printHexBinary(resultArr);
        byte[] resultArrCompressed = NativeSecp256k1.pubKeyTweakMul( pub , data, true);
        String pubkeyStringCompressed = DatatypeConverter.printHexBinary(resultArrCompressed);
        assertEquals(pubkeyString , "04E0FE6FE55EBCA626B98A807F6CAF654139E14E5E3698F01A9A658E21DC1D2791EC060D4F412A794D5370F672BC94B722640B5F76914151CFCA6E712CA48CC589" , "testPubKeyTweakMul");
        assertEquals(pubkeyStringCompressed , "03E0FE6FE55EBCA626B98A807F6CAF654139E14E5E3698F01A9A658E21DC1D2791" , "testPubKeyTweakMul (compressed)");
    }

    /**
      * This tests seed randomization
      */
    @Test
    public void testRandomize() throws AssertFailException {
        byte[] seed = DatatypeConverter.parseHexBinary("A441B15FE9A3CF56661190A0B93B9DEC7D04127288CC87250967CF3B52894D11"); //sha256hash of "random"
        boolean result = NativeSecp256k1.randomize(seed);
        assertEquals( result, true, "testRandomize");
    }

    /**
     * Tests that we can decompress valid public keys
     * @throws AssertFailException
     */
    @Test
    public void testDecompressPubKey() throws AssertFailException {
        byte[] compressedPubKey = DatatypeConverter.parseHexBinary("0315EAB529E7D5EB637214EA8EC8ECE5DCD45610E8F4B7CC76A35A6FC27F5DD981");

        byte[] result1 = NativeSecp256k1.decompress(compressedPubKey);
        byte[] result2 = NativeSecp256k1.decompress(result1); // this is a no-op
        String resultString1 = DatatypeConverter.printHexBinary(result1);
        String resultString2 = DatatypeConverter.printHexBinary(result2);
        assertEquals(resultString1, "0415EAB529E7D5EB637214EA8EC8ECE5DCD45610E8F4B7CC76A35A6FC27F5DD9817551BE3DF159C83045D9DFAC030A1A31DC9104082DB7719C098E87C1C4A36C19", "testDecompressPubKey (compressed)");
        assertEquals(resultString2, "0415EAB529E7D5EB637214EA8EC8ECE5DCD45610E8F4B7CC76A35A6FC27F5DD9817551BE3DF159C83045D9DFAC030A1A31DC9104082DB7719C098E87C1C4A36C19", "testDecompressPubKey (no-op)");
    }

    /**
     * Tests that we can check validity of public keys
     * @throws AssertFailException
     */
    @Test
    public void testIsValidPubKeyPos() throws AssertFailException {
        byte[] pubkey = DatatypeConverter.parseHexBinary("0456b3817434935db42afda0165de529b938cf67c7510168a51b9297b1ca7e4d91ea59c64516373dd2fe6acc79bb762718bc2659fa68d343bdb12d5ef7b9ed002b");
        byte[] compressedPubKey = DatatypeConverter.parseHexBinary("03de961a47a519c5c0fc8e744d1f657f9ea6b9a921d2a3bceb8743e1885f752676");

        boolean result1 = NativeSecp256k1.isValidPubKey(pubkey);
        boolean result2 = NativeSecp256k1.isValidPubKey(compressedPubKey);
        assertEquals(result1, true, "testIsValidPubKeyPos");
        assertEquals(result2, true, "testIsValidPubKeyPos (compressed)");
    }
    @Test
    public void testIsValidPubKeyNeg() throws AssertFailException {
        //do we have test vectors some where to test this more thoroughly?
        byte[] pubkey = DatatypeConverter.parseHexBinary("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

        boolean result1 = NativeSecp256k1.isValidPubKey(pubkey);
        assertEquals(result1, false, "testIsValidPubKeyNeg");
    }

    /**
     * This tests signSchnorr() for a valid secretkey
     */
    @Test
    public void testSchnorrSign() throws AssertFailException{
        byte[] data = DatatypeConverter.parseHexBinary("5255683DA567900BFD3E786ED8836A4E7763C221BF1AC20ECE2A5171B9199E8A"); //sha256(sha256("Very deterministic message"))
        byte[] sec = DatatypeConverter.parseHexBinary("12B004FFF7F4B69EF8650E767F18F11EDE158148B425660723B9F9A66E61F747");

        byte[] resultArr = NativeSecp256k1.schnorrSign(data, sec);
        String sigString = DatatypeConverter.printHexBinary(resultArr);
        assertEquals( sigString, "2C56731AC2F7A7E7F11518FC7722A166B02438924CA9D8B4D111347B81D0717571846DE67AD3D913A8FDF9D8F3F73161A4C48AE81CB183B214765FEB86E255CE" , "testSchnorrSign");
    }

    /**
     * This tests schnorrVerify() for a valid signature
     */
    @Test
    public void testSchnorrVerifyPos() throws AssertFailException{
        byte[] data0 = DatatypeConverter.parseHexBinary("0000000000000000000000000000000000000000000000000000000000000000");
        byte[] sig0 = DatatypeConverter.parseHexBinary("787A848E71043D280C50470E8E1532B2DD5D20EE912A45DBDD2BD1DFBF187EF67031A98831859DC34DFFEEDDA86831842CCD0079E1F92AF177F7F22CC1DCED05");
        byte[] pub0 = DatatypeConverter.parseHexBinary("0279BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798");
        boolean result0 = NativeSecp256k1.schnorrVerify(data0, sig0, pub0);
        assertEquals( result0, true , "testSchnorrVerifyPos0");

        byte[] data1 = DatatypeConverter.parseHexBinary("243F6A8885A308D313198A2E03707344A4093822299F31D0082EFA98EC4E6C89");
        byte[] sig1 = DatatypeConverter.parseHexBinary("2A298DACAE57395A15D0795DDBFD1DCB564DA82B0F269BC70A74F8220429BA1D1E51A22CCEC35599B8F266912281F8365FFC2D035A230434A1A64DC59F7013FD");
        byte[] pub1 = DatatypeConverter.parseHexBinary("02DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659");
        boolean result1 = NativeSecp256k1.schnorrVerify(data1, sig1, pub1);
        assertEquals( result1, true , "testSchnorrVerifyPos1");

        byte[] data2 = DatatypeConverter.parseHexBinary("5E2D58D8B3BCDF1ABADEC7829054F90DDA9805AAB56C77333024B9D0A508B75C");
        byte[] sig2 = DatatypeConverter.parseHexBinary("00DA9B08172A9B6F0466A2DEFD817F2D7AB437E0D253CB5395A963866B3574BE00880371D01766935B92D2AB4CD5C8A2A5837EC57FED7660773A05F0DE142380");
        byte[] pub2 = DatatypeConverter.parseHexBinary("03FAC2114C2FBB091527EB7C64ECB11F8021CB45E8E7809D3C0938E4B8C0E5F84B");
        boolean result2 = NativeSecp256k1.schnorrVerify(data2, sig2, pub2);
        assertEquals( result2, true , "testSchnorrVerifyPos2");

    }

    /**
     * This tests schnorrVerify() for a invalid signature
     */
    @Test
    public void testSchnorrVerifyNeg() throws AssertFailException{
        byte[] data = DatatypeConverter.parseHexBinary("4DF3C3F68FCC83B27E9D42C90431A72499F17875C81A599B566C9889B9696703");
        byte[] sig = DatatypeConverter.parseHexBinary("00000000000000000000003B78CE563F89A0ED9414F5AA28AD0D96D6795F9C6302A8DC32E64E86A333F20EF56EAC9BA30B7246D6D25E22ADB8C6BE1AEB08D49D");
        byte[] pub = DatatypeConverter.parseHexBinary("03EEFDEA4CDB677750A420FEE807EACF21EB9898AE79B9768766E4FAA04A2D4A34");
        boolean result = NativeSecp256k1.schnorrVerify(data, sig, pub);
        assertEquals( result, false , "testSchnorrVerifyNeg");
    }

    @Test
    public void testCreateECDHSecret() throws AssertFailException{
        byte[] sec = DatatypeConverter.parseHexBinary("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530");
        byte[] pub = DatatypeConverter.parseHexBinary("040A629506E1B65CD9D2E0BA9C75DF9C4FED0DB16DC9625ED14397F0AFC836FAE595DC53F8B0EFE61E703075BD9B143BAC75EC0E19F82A2208CAEB32BE53414C40");

        byte[] resultArr = NativeSecp256k1.createECDHSecret(sec, pub);
        String ecdhString = DatatypeConverter.printHexBinary(resultArr);
        assertEquals( ecdhString, "2A2A67007A926E6594AF3EB564FC74005B37A9C8AEF2033C4552051B5C87F043" , "testCreateECDHSecret");
    }

}
