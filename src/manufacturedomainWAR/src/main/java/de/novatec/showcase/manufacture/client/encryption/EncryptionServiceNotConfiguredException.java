package de.novatec.showcase.manufacture.client.encryption;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EncryptionServiceNotConfiguredException extends Exception {

	private static final long serialVersionUID = -2605722900558101557L;

	public EncryptionServiceNotConfiguredException() {
		super();
	}

	public EncryptionServiceNotConfiguredException(String message) {
		super(message);
	}

}
