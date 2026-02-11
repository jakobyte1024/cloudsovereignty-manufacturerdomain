package de.novatec.showcase.manufacture.ejb.encryptor;

import de.novatec.showcase.manufacture.client.decryption.DecryptionBatchRequest;
import de.novatec.showcase.manufacture.client.decryption.DecryptionServiceClient;
import de.novatec.showcase.manufacture.client.decryption.DecryptionServiceNotConfiguredException;
import de.novatec.showcase.manufacture.client.decryption.PlaintextResponse;
import de.novatec.showcase.manufacture.ejb.entity.Bom;
import de.novatec.showcase.manufacture.utils.EncryptionUtils;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

public class BomBatchDecryptSessionListener extends SessionEventAdapter {

    private static final DecryptionServiceClient CLIENT;

    static {
        try {
            CLIENT = new DecryptionServiceClient();
        } catch (DecryptionServiceNotConfiguredException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postExecuteQuery(SessionEvent event) {
        if (!(event.getQuery() instanceof ReadAllQuery)) {
            return;
        }

        Class<?> referenceClass = event.getQuery().getReferenceClass();
        if (referenceClass != Bom.class) {
            return;
        }

        List<?> results = (List<?>) event.getResult();
        if (results == null || results.isEmpty()) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            List<Bom> bomList = (List<Bom>) results;
            batchDecryptBoms(bomList);
        } catch (Exception e) {
            throw new PersistenceException("Error decrypting BOM batch", e);
        }
    }

    private void batchDecryptBoms(List<Bom> entities) {
        int size = entities.size();

        // extract Data-Encryption-Keys
        List<DecryptionBatchRequest.BatchDecryptItem> batchInputs = new ArrayList<>(size);

        for (Bom b : entities) {
            String encKey = b.getDataEncryptionKeyEncrypted();
            if (encKey != null) {
                batchInputs.add(new DecryptionBatchRequest.BatchDecryptItem(encKey));
            } else {
                batchInputs.add(new DecryptionBatchRequest.BatchDecryptItem(""));
            }
        }

        DecryptionBatchRequest dekRequest = new DecryptionBatchRequest();
        dekRequest.setKeyProvider("vault");
        dekRequest.setKeyName("kek");
        dekRequest.setData(batchInputs);

        // decrypt Data-Encryption-Keys
        List<PlaintextResponse> dekDecrypted = null;
        try {
            dekDecrypted = CLIENT.decryptBatch(dekRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (dekDecrypted.size() != size) {
            throw new PersistenceException("Decryption service returned mismatched result count");
        }

        // Apply Decryption
        for (int i = 0; i < size; i++) {
            Bom bom = entities.get(i);
            PlaintextResponse response = dekDecrypted.get(i);

            if (response == null) {
                continue;
            }

            byte[] dataEncryptionKey = response.as(byte[].class);
            if (dataEncryptionKey == null) continue;

            try {

                if (bom.getQuantityEncrypted() != null) {
                    String val = EncryptionUtils.decryptWithDEK(dataEncryptionKey, bom.getQuantityEncrypted());
                    bom.setQuantity(Integer.parseInt(val));
                }

                if (bom.getEngChangeEncrypted() != null) {
                    bom.setEngChange(EncryptionUtils.decryptWithDEK(dataEncryptionKey, bom.getEngChangeEncrypted()));
                }

                if (bom.getOpsDescEncrypted() != null) {
                    bom.setOpsDesc(EncryptionUtils.decryptWithDEK(dataEncryptionKey, bom.getOpsDescEncrypted()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}