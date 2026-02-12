package de.novatec.showcase.manufacture.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

public class EncryptionUtils {
    private static final String AES_ALGORITHM = "AES";

    public static byte[] generateDEK() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(128); // AES-128
            SecretKey secretKey = keyGen.generateKey();
            return secretKey.getEncoded();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String encryptWithDEK(byte[] dek, String plaintext) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(dek, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printBase64Binary(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public static String decryptWithDEK(byte[] dek, String ciphertext) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(dek, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = DatatypeConverter.parseBase64Binary(ciphertext);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
