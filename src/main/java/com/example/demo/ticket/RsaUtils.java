package com.example.demo.ticket;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaUtils {
    private static final String TYPE = "RSA";

    private static final String ALGORITHM = "RSA/ECB/PKCS1PADDING";

    private static final String CHARSET = "UTF-8";

    private static final int KEY_SIZE = 1024;

    public static KeyPair createKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(TYPE);

            keyPairGenerator.initialize(KEY_SIZE);

            return keyPairGenerator.generateKeyPair();

        } catch (Exception e) {
            return null;

        }

    }

    public static String getPublicKey(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    }

    public static String getPrivateKey(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

    }

    public static String encrypt(String data, String publicKeyString) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        KeyFactory keyFactory = KeyFactory.getInstance(TYPE);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString));

        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return Base64.getEncoder().encodeToString(splitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET)));

    }

    public static String decrypt(String data, String privateKeyString) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        KeyFactory keyFactory = KeyFactory.getInstance(TYPE);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString));

        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(splitCodec(cipher, Cipher.DECRYPT_MODE, Base64.getDecoder().decode(data)), CHARSET);

    }

    private static byte[] splitCodec(Cipher cipher, int mode, byte[] data) throws Exception {
        int maxBlock = KEY_SIZE / 8 - (mode == Cipher.DECRYPT_MODE ? 0 : 11);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer;

            for (int offset = 0; offset < data.length; offset += maxBlock) {
                buffer = cipher.doFinal(data, offset, Math.min(maxBlock, data.length - offset));

                out.write(buffer, 0, buffer.length);

            }

            return out.toByteArray();

        }

    }

}