package de.novatec.showcase.manufacture.ejb.entity;

import de.novatec.showcase.manufacture.ejb.encryptor.EncryptedEntityDescriptorListener;

import javax.persistence.*;
import java.util.Map;

@MappedSuperclass
@EntityListeners(EncryptedEntityDescriptorListener.class)
public abstract class AbstractEncryptedEntity {

    @Column(name = "DEK")
    private String dataEncryptionKeyEncrypted;

    /**
     * Called by the Listener @PrePersist/@PreUpdate.
     * The child class must take its plain fields (e.g., quantity)
     * and write them into the encrypted fields (e.g., quantityEncrypted)
     * using the provided key.
     */
    public abstract void encryptData(byte[] dek);

    /**
     * Called by the Session Listener
     * The child class must take its encrypted fields
     * and write them into the transient plain fields
     * using the provided key.
     */
    public abstract void decryptData(byte[] dek);

    /**
     * Called by the encryption process to get metadata about the entity
     * @return Metadata about the entity (visible when en- and decrypting)
     */
    public abstract Map<String, Object> getEncryptionMetadata();

    public String getDataEncryptionKeyEncrypted() {
        return dataEncryptionKeyEncrypted;
    }

    public void setDataEncryptionKeyEncrypted(String dataEncryptionKeyEncrypted) {
        this.dataEncryptionKeyEncrypted = dataEncryptionKeyEncrypted;
    }
}