package de.novatec.showcase.manufacture.client.decryption;

import java.util.ArrayList;
import java.util.List;

public class DecryptionBatchRequest {

    private String keyProvider;
    private String keyName;
    private List<BatchDecryptItem> data; // mandatory

    public DecryptionBatchRequest() {
        this.data = new ArrayList<BatchDecryptItem>();
    }

    public String getKeyProvider() { return keyProvider; }
    public void setKeyProvider(String keyProvider) { this.keyProvider = keyProvider; }

    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }

    public List<BatchDecryptItem> getData() { return data; }
    public void setData(List<BatchDecryptItem> data) { this.data = data; }

    public static class BatchDecryptItem {
        private String ciphertext;

        public BatchDecryptItem(String ciphertext) {
            this.ciphertext = ciphertext;
        }

        public String getCiphertext() { return ciphertext; }
        public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }
    }
}

