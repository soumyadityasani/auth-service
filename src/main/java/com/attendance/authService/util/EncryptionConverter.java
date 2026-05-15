package com.attendance.authService.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
@Component  // ✅ Must be @Component to inject values
public class EncryptionConverter implements AttributeConverter<String, String> {

    // ✅ Reads from active profile's properties
    @Value("${ENCRYPT_VALUE}")
    private boolean encryptionEnabled;

    @Value("${AES_SECRET_KEY}")
    private String secretKey;

    @Value("${AES_INIT_VECTOR}")
    private String initVector;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null) return null;

        // ✅ DEV — save as plain text, easy to read in DB
        if (!encryptionEnabled) return plainText;

        // ✅ PROD — encrypt before saving
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            return Base64.getEncoder().encodeToString(
                    cipher.doFinal(plainText.getBytes("UTF-8"))
            );
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String storedValue) {
        if (storedValue == null) return null;

        // ✅ DEV — already plain text, just return it
        if (!encryptionEnabled) return storedValue;

        // ✅ PROD — decrypt after fetching
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            return new String(cipher.doFinal(Base64.getDecoder().decode(storedValue)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
