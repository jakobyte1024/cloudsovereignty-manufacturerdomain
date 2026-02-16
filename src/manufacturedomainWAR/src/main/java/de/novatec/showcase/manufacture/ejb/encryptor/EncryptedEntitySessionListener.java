package de.novatec.showcase.manufacture.ejb.encryptor;

import de.novatec.showcase.manufacture.client.decryption.DecryptionBatchRequest;
import de.novatec.showcase.manufacture.client.decryption.DecryptionBatchRequest.BatchDecryptItem;
import de.novatec.showcase.manufacture.client.decryption.DecryptionServiceClient;
import de.novatec.showcase.manufacture.client.decryption.DecryptionServiceNotConfiguredException;
import de.novatec.showcase.manufacture.client.decryption.PlaintextResponse;
import de.novatec.showcase.manufacture.ejb.entity.AbstractEncryptedEntity;
import de.novatec.showcase.manufacture.utils.EncryptionConfig;

import org.eclipse.persistence.queries.ReadQuery;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EncryptedEntitySessionListener extends SessionEventAdapter {

    private static final DecryptionServiceClient CLIENT;

    static {
        try {
            CLIENT = new DecryptionServiceClient();
        } catch (DecryptionServiceNotConfiguredException e) {
            throw new RuntimeException("Encryption Service not configured", e);
        }
    }

    @Override
    public void postExecuteQuery(SessionEvent event) {
        // check if query is ReadQuery (covers ReadAllQuery and ReadObjectQuery)
        if (!(event.getQuery() instanceof ReadQuery)) {
            return;
        }

        // check if the entity being queried extends AbstractEncryptedEntity
        Class<?> referenceClass = event.getQuery().getReferenceClass();
        if (referenceClass == null || !AbstractEncryptedEntity.class.isAssignableFrom(referenceClass)) {
            return;
        }

        Object result = event.getResult();
        if (result == null) {
            return;
        }

        // normalize result to a List
        List<AbstractEncryptedEntity> entitiesToDecrypt;

        if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<AbstractEncryptedEntity> list = (List<AbstractEncryptedEntity>) result;
            entitiesToDecrypt = list;
        } else if (result instanceof AbstractEncryptedEntity) {
            entitiesToDecrypt = Collections.singletonList((AbstractEncryptedEntity) result);
        } else {
            // result is not expected
            return;
        }

        if (entitiesToDecrypt.isEmpty()) {
            return;
        }

        // Perform Decryption
        try {
            batchDecryptEntities(entitiesToDecrypt);
        } catch (Exception e) {
            throw new PersistenceException("Error decrypting entity batch", e);
        }
    }

    private void batchDecryptEntities(List<AbstractEncryptedEntity> entities) {
        int size = entities.size();
        List<BatchDecryptItem> batchInputs = new ArrayList<>(size);

        // collect encrypted deks from entities
        for (AbstractEncryptedEntity entity : entities) {
            String encKey = entity.getDataEncryptionKeyEncrypted();
            // Handle null keys gracefully (e.g. new un-persisted entities or legacy data)
            if (encKey != null) {
                batchInputs.add(new BatchDecryptItem(encKey));
            } else {
                batchInputs.add(new BatchDecryptItem("")); // Placeholder to keep index alignment
            }
        }

        DecryptionBatchRequest dekRequest = new DecryptionBatchRequest();
        dekRequest.setKeyProvider(EncryptionConfig.getKekProvider(entities.get(0).getClass()));
        dekRequest.setKeyName(EncryptionConfig.getKekKeyName(entities.get(0).getClass()));
        dekRequest.setData(batchInputs);

        // call decryption-service
        List<PlaintextResponse> dekDecrypted;
        try {
            dekDecrypted = CLIENT.decryptBatch(dekRequest);
        } catch (Exception e) {
            throw new RuntimeException("Remote decryption service failed", e);
        }

        if (dekDecrypted.size() != size) {
            throw new PersistenceException("Decryption service returned mismatched result count. Sent: " + size + ", Received: " + dekDecrypted.size());
        }

        // apply decryption to entity data
        for (int i = 0; i < size; i++) {
            AbstractEncryptedEntity entity = entities.get(i);
            PlaintextResponse response = dekDecrypted.get(i);

            if (response == null || entity.getDataEncryptionKeyEncrypted() == null) {
                continue;
            }

            byte[] dataEncryptionKey = response.as(byte[].class);

            if (dataEncryptionKey != null) {
                entity.decryptData(dataEncryptionKey);
            }
        }
    }
}