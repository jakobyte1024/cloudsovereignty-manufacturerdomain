package de.novatec.showcase.manufacture.client.decryption;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

public class PlaintextResponse {
    private String plaintext;

    public PlaintextResponse() {}

    public PlaintextResponse(String plaintext, Object metadata) {
        this.plaintext = plaintext;
    }

    public String getPlaintext() { return plaintext; }
    public void setPlaintext(String plaintext) { this.plaintext = plaintext; }

    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> clazz) {
        if (plaintext == null) return null;

        try {
            // Decode Base64
            byte[] decoded = DatatypeConverter.parseBase64Binary(plaintext);
            String value = new String(decoded, StandardCharsets.UTF_8);

            if (clazz == String.class) {
                return (T) value;
            } else if (clazz == Integer.class) {
                return (T) Integer.valueOf(value);
            } else if (clazz == Long.class) {
                return (T) Long.valueOf(value);
            } else if (clazz == Boolean.class) {
                return (T) Boolean.valueOf(value);
            } else if (clazz == Double.class) {
                return (T) Double.valueOf(value);
            } else if (clazz == byte[].class) {
                return (T) decoded;
            }

            throw new IllegalArgumentException("Unsupported type: " + clazz.getName());

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert plaintext to " + clazz.getName(), e);
        }
    }

}