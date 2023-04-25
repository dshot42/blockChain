package com.chiffrement;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ChiffrementUtils {

    static String algorithm = "AES/CBC/PKCS5Padding";
    static byte[] defaultKey = new byte[]{-95, -14, 120, 61, 45, 104, 101, -13, -98, -20, -69, -41, -97, 83, 46, 75, -104, 105, -3, 111, -125, -90, -11, -8, 60, 69, 38, -33, 78, 55, -65, 104};
    // System.out.println(" key   " + Hex.encodeHex(key));
    static SecretKey secretKey = new SecretKeySpec(defaultKey, 0, defaultKey.length, "AES");

    static byte[] iv = {-92, -101, -41, -27, -61, -44, 29, 82, -121, 11, -77, 9, -106, 15, -100, -55};
    // System.out.println(" iv   " + Hex.encodeHex(iv));

    static IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    protected static String getClientDatas(String address2String, String hostName2String) throws DecoderException, UnsupportedEncodingException {
        byte[] hardwareAddress = Hex.decodeHex(address2String.replace("-", ""));
        byte[] hostName = hostName2String.getBytes("US-ASCII");
        String hardwareAddress16 = Hex.encodeHexString(hardwareAddress, false);
        String host16 = Hex.encodeHexString(hostName, false);
        return hardwareAddress16 + host16;
    }

    public static String getOwnDatas() throws UnknownHostException, SocketException, UnsupportedEncodingException {
        InetAddress localHost = InetAddress.getLocalHost();
        NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
        byte[] hardwareAddress = ni.getHardwareAddress();
        byte[] hostName = InetAddress.getLocalHost().getHostName().getBytes("US-ASCII");
        System.out.println("address =" + getDatas16(hardwareAddress, true));
        System.out.println("host =" + InetAddress.getLocalHost().getHostName());
        String hardwareAddress16 = Hex.encodeHexString(hardwareAddress, false);
        String host16 = Hex.encodeHexString(hostName, false);
        return hardwareAddress16 + host16;
    }

    public static String getDatas16(byte[] hardwareAddress, boolean address) {
        String hardwareAddress16 = "";
        int i = 0;
        for (byte b : hardwareAddress) {
            if (address && i > 0) hardwareAddress16 += "-";
            hardwareAddress16 += String.format("%02X", b);
            i++;
        }
        return hardwareAddress16;
    }

    public static boolean hashCompare(String userHash) throws Exception {
        return userHash.equals(generateHashKey(getOwnDatas()));
    }

    public static String cryptAES(String datas) throws Exception {

         SecretKey secretKey = new SecretKeySpec(defaultKey, 0, defaultKey.length, "AES");
        try {
            // openssl enc -aes-256-cbc -k secret -P -md sha1
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] cipherText = cipher.doFinal(datas.getBytes());
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        } catch (Exception e) {
            System.out.println("error generate AES cipher");
            throw new Exception(e);
        }
    }

    public static String decryptAES(String datas) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE,  secretKey, ivParameterSpec);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(datas));
        return new String(plainText);
    }

    public static String generateHashKey(String datas) throws Exception {
        String hashKey = DigestUtils.sha256Hex(cryptAES(datas));
        String keygen = "";
        for (int i = 0; i < hashKey.length(); i += 8) {
            if (i > 0 && i % 8 == 0) keygen += "-";
            keygen += hashKey.substring(i, i + 8);
        }
        return keygen.toUpperCase();
    }


    public static String cryptAES(String datas,byte[] key) throws Exception {

        SecretKey secretKey = new SecretKeySpec(key, 0, defaultKey.length, "AES");
        try {
            // openssl enc -aes-256-cbc -k secret -P -md sha1
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] cipherText = cipher.doFinal(datas.getBytes());
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        } catch (Exception e) {
            System.out.println("error generate AES cipher");
            throw new Exception(e);
        }
    }

    public static String decryptAES(String datas,byte[] key) throws Exception{
        SecretKey secretKey = new SecretKeySpec(key, 0, defaultKey.length, "AES");
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE,  secretKey, ivParameterSpec);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(datas));
        return new String(plainText);
    }
}
