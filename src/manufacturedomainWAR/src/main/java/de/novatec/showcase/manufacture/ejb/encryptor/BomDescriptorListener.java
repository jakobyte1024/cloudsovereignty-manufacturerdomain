package de.novatec.showcase.manufacture.ejb.encryptor;

import de.novatec.showcase.manufacture.client.encryption.CiphertextResponse;
import de.novatec.showcase.manufacture.client.encryption.EncryptionRequest;
import de.novatec.showcase.manufacture.client.encryption.EncryptionServiceClient;
import de.novatec.showcase.manufacture.ejb.entity.Bom;
import de.novatec.showcase.manufacture.utils.EncryptionUtils;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BomDescriptorListener {

    @PrePersist
    @PreUpdate
    public void encrypt(Object entity) {
        if (entity instanceof Bom) {
            final Bom bom = (Bom) entity;

            try {
                byte[] dataEncryptionKey = EncryptionUtils.generateDEK();
                List<EncryptionRequest.ItemWithMetadata> batch = new ArrayList<>(1);

                Map<String, Object> metadata = new HashMap<String, Object>() {{
                    put("lineNo", bom.getLineNo());
                    put("assemblyId", bom.getAssemblyId());
                    put("componentId", bom.getComponentId());
                }};
                batch.add(new EncryptionRequest.ItemWithMetadata(dataEncryptionKey, metadata));

                EncryptionServiceClient client = new EncryptionServiceClient();
                EncryptionRequest request = EncryptionRequest.fromBatch(
                        "vault",  // KEK provider
                        "kek",              // KEK key name
                        null,               // optional key version
                        batch               // DEK batch
                );

                List<CiphertextResponse> responses = client.encrypt(request);
                String encryptedDek = responses.get(0).getCiphertext();

                String quantityEncrypted = EncryptionUtils.encryptWithDEK(dataEncryptionKey, Integer.toString(bom.getQuantity()));
                String engChangeEncrypted = EncryptionUtils.encryptWithDEK(dataEncryptionKey, bom.getEngChange());
                String opsDescEncrypted = EncryptionUtils.encryptWithDEK(dataEncryptionKey, bom.getOpsDesc());

                bom.setDataEncryptionKeyEncrypted(encryptedDek);
                bom.setQuantityEncrypted(quantityEncrypted);
                bom.setEngChangeEncrypted(engChangeEncrypted);
                bom.setOpsDescEncrypted(opsDescEncrypted);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
