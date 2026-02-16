package de.novatec.showcase.manufacture.ejb.encryptor;

import de.novatec.showcase.manufacture.client.encryption.CiphertextResponse;
import de.novatec.showcase.manufacture.client.encryption.EncryptionRequest;
import de.novatec.showcase.manufacture.client.encryption.EncryptionServiceClient;
import de.novatec.showcase.manufacture.ejb.entity.AbstractEncryptedEntity;
import de.novatec.showcase.manufacture.utils.EncryptionConfig;
import de.novatec.showcase.manufacture.utils.EncryptionUtils;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EncryptedEntityDescriptorListener {

    private volatile EncryptionServiceClient _client;

    public EncryptedEntityDescriptorListener() {}

    /**
     * Thread-safe accessor using Double-Checked Locking
     */
    private EncryptionServiceClient getClient() {
        EncryptionServiceClient localRef = _client;
        if (localRef == null) {
            synchronized (this) {
                localRef = _client;
                if (localRef == null) {
                    try {
                        localRef = new EncryptionServiceClient();
                        _client = localRef;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize EncryptionServiceClient", e);
                    }
                }
            }
        }

        return localRef;
    }

    @PrePersist
    @PreUpdate
    public void encrypt(Object entity) {
        // Check if this is an entity we know how to handle
        if (!(entity instanceof AbstractEncryptedEntity)) {
            return;
        }

        EncryptionServiceClient client = getClient();

        AbstractEncryptedEntity encryptedEntity = (AbstractEncryptedEntity) entity;

        try {
            // generate new dek
            byte[] dataEncryptionKey = EncryptionUtils.generateDEK();

            // prepare metadata
            Map<String, Object> metadata = encryptedEntity.getEncryptionMetadata();

            // create single batch
            List<EncryptionRequest.ItemWithMetadata> batch = new ArrayList<>(1);
            batch.add(new EncryptionRequest.ItemWithMetadata(dataEncryptionKey, metadata));

            EncryptionRequest request = EncryptionRequest.fromBatch(
                    EncryptionConfig.getKekProvider(encryptedEntity.getClass()),
                    EncryptionConfig.getKekKeyName(encryptedEntity.getClass()),
                    null,               // optional key version (null = latest)
                    batch               // DEK batch
            );

            List<CiphertextResponse> responses = client.encrypt(request);
            String encryptedDek = responses.get(0).getCiphertext();

            encryptedEntity.setDataEncryptionKeyEncrypted(encryptedDek);
            encryptedEntity.encryptData(dataEncryptionKey);

            // zero the content of the dataEncryptionKey array for security
            if (dataEncryptionKey != null) {
                Arrays.fill(dataEncryptionKey, (byte) 0);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Encryption failed", ex);
        }
    }
}