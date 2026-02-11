package de.novatec.showcase.manufacture.client.decryption;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class DecryptionServiceNotConfiguredException extends Exception {

	private static final long serialVersionUID = -2605722900558101557L;

	public DecryptionServiceNotConfiguredException() {
		super();
	}

	public DecryptionServiceNotConfiguredException(String message) {
		super(message);
	}

}
