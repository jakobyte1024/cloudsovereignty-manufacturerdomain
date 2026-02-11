package de.novatec.showcase.manufacture.client.encryption;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import de.novatec.showcase.manufacture.client.RestcallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ManagedBean;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@ManagedBean
public class EncryptionServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(EncryptionServiceClient.class);

    private static final String JNDI_PROPERTY_ENCRYPTION_URL = "encryptionproxy.url.encrypt";
    private static final String JNDI_PROPERTY_SECRET = "encryptionproxy.secret";

    private String encryptionUrl;
    private String secret;

    private Client client;

    public EncryptionServiceClient() throws EncryptionServiceNotConfiguredException {

        client = ClientBuilder.newClient();
        client.register(JacksonJsonProvider.class);

        try {
            InitialContext ctx = new InitialContext();
            encryptionUrl = (String) ctx.lookup(JNDI_PROPERTY_ENCRYPTION_URL);
            secret = (String) ctx.lookup(JNDI_PROPERTY_SECRET);
        } catch (NamingException e) {
            LOG.error("Missing JNDI encryption properties", e);
            throw new EncryptionServiceNotConfiguredException("Encryption service not configured correctly!");
        }

        if (validateJNDIProperty(encryptionUrl) || validateJNDIProperty(secret)) {
            throw new EncryptionServiceNotConfiguredException(
                    "Encryption service properties missing in server.env!");
        }
    }

    /**
     * Encrypts a batch of items. The request must contain a list of items.
     */
    public List<CiphertextResponse> encrypt(EncryptionRequest request) throws RestcallException {

        WebTarget target = client.target(encryptionUrl);

        try (Response response = asBearer(target.request(MediaType.APPLICATION_JSON_TYPE))
                .post(Entity.json(request))) {

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(new GenericType<List<CiphertextResponse>>() {});
            }

            String errorBody = response.readEntity(String.class);
            throw new RestcallException("Error "
                    + Response.Status.fromStatusCode(response.getStatus())
                    + " while calling " + encryptionUrl
                    + ". Response: " + errorBody);
        }
    }

    /**
     * Adds Authorization: Bearer header
     */
    private Builder asBearer(Builder builder) {
        return builder.header("Authorization", "Bearer " + secret);
    }

    private boolean validateJNDIProperty(String value) {
        return value == null || value.startsWith("${env.");
    }
}
