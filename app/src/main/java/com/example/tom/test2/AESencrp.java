package com.example.tom.test2;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESencrp {

    private static final String ALGO = "AES";


    public static byte[] encrypt(byte[] Data, final byte[] keyValue) throws Exception {
        byte[] bytes = new byte[16];
        Arrays.fill( bytes, (byte) 8 );
        for(int i = 0; i < keyValue.length -1;i++){
            bytes[i] = keyValue[i];
        }
        Key key = generateKey(bytes);
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data);
        //String encryptedValue = new BASE64Encoder().encode(encVal);
        return encVal;
    }

    public static byte[] decrypt(byte[] encryptedData, final byte[] keyValue) throws Exception {
        byte[] bytes = new byte[16];
        Arrays.fill( bytes, (byte) 8 );
        for(int i = 0; i < keyValue.length -1;i++){
            bytes[i] = keyValue[i];
        }
        Key key = generateKey(bytes);
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);

        byte[] decValue = c.doFinal(encryptedData);
        return decValue;
    }

    private static Key generateKey(final byte[] keyValue) throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;

    }
}