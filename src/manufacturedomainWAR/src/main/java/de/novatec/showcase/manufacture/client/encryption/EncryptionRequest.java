package de.novatec.showcase.manufacture.client.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EncryptionRequest {

    private static final Logger LOG = LoggerFactory.getLogger(EncryptionRequest.class);

    private String keyProvider;
    private String keyName;
    private Integer keyVersion;
    private List<BatchEncryptItem> data;

    private EncryptionRequest() {
        this.data = new ArrayList<>();
    }

    /**
     * Build a batch encryption request
     *
     * @param keyProvider Key provider
     * @param keyName Key name
     * @param keyVersion Key version
     * @param items List of objects to encrypt, each with its own metadata
     * @return EncryptionRequest
     */
    public static EncryptionRequest fromBatch(
            String keyProvider,
            String keyName,
            Integer keyVersion,
            List<ItemWithMetadata> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Batch items must not be null or empty");
        }

        EncryptionRequest request = new EncryptionRequest();
        request.keyProvider = keyProvider;
        request.keyName = keyName;
        request.keyVersion = keyVersion;

        for (ItemWithMetadata item : items) {
            if (item == null || item.getData() == null) {
                throw new IllegalArgumentException("Each batch item and its data must not be null");
            }

            try {
                request.data.add(new BatchEncryptItem(item.getDataBase64(), item.getMetadata()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to encode batch item", e);
            }
        }

        return request;
    }

    // getters / setters
    public String getKeyProvider() { return keyProvider; }
    public void setKeyProvider(String keyProvider) { this.keyProvider = keyProvider; }

    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }

    public Integer getKeyVersion() { return keyVersion; }
    public void setKeyVersion(Integer keyVersion) { this.keyVersion = keyVersion; }

    public List<BatchEncryptItem> getData() { return data; }
    public void setData(List<BatchEncryptItem> data) { this.data = data; }

    public static class BatchEncryptItem {

        private final String plaintext;
        private final Object metadata;

        public BatchEncryptItem(String plaintext, Object metadata) {
            this.plaintext = plaintext;
            this.metadata = metadata;
        }

        public String getPlaintext() { return plaintext; }
        public Object getMetadata() { return metadata; }
    }

    public static class ItemWithMetadata {
        private final Object data;
        private final Object metadata;

        public ItemWithMetadata(Object data, Object metadata) {
            this.data = data;
            this.metadata = metadata;
        }

        public Object getData() { return data; }
        public Object getMetadata() { return metadata; }

        public String getDataBase64() {
            if (data == null) {
                return null;
            }

            byte[] bytes;

            if (data instanceof byte[]) {
                bytes = (byte[]) data; // already a byte array
            } else if (data instanceof String) {
                bytes = ((String) data).getBytes(StandardCharsets.UTF_8); // encode UTF-8
            } else {
                bytes = data.toString().getBytes(StandardCharsets.UTF_8); // fallback
            }

            return DatatypeConverter.printBase64Binary(bytes);
        }
    }
}