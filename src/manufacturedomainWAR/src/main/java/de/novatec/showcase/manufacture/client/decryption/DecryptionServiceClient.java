package de.novatec.showcase.manufacture.client.decryption;

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
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ManagedBean
public class DecryptionServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(DecryptionServiceClient.class);

    private static final String JNDI_PROPERTY_DECRYPTION_URL = "encryptionproxy.url.decrypt";
    private static final String JNDI_PROPERTY_SECRET = "encryptionproxy.secret";

    private String decryptionUrl;
    private String secret;

    private Client client;

    public DecryptionServiceClient() throws DecryptionServiceNotConfiguredException {
        client = ClientBuilder.newClient();

        try {
            InitialContext ctx = new InitialContext();
            decryptionUrl = (String) ctx.lookup(JNDI_PROPERTY_DECRYPTION_URL);
            secret = (String) ctx.lookup(JNDI_PROPERTY_SECRET);
        } catch (NamingException e) {
            LOG.error("Missing JNDI decryption properties", e);
            throw new DecryptionServiceNotConfiguredException("Decryption service not configured properly!");
        }

        if (validateJNDIProperty(decryptionUrl) || validateJNDIProperty(secret)) {
            throw new DecryptionServiceNotConfiguredException(
                    "Decryption service properties missing in server.env!");
        }
    }

    /**
     * Decrypts a batch of items. The request must contain a list of items.
     */
    public List<PlaintextResponse> decryptBatch(DecryptionBatchRequest request) throws Exception {

        if (request == null || request.getData() == null || request.getData().isEmpty()) {
            throw new IllegalArgumentException("Batch request must contain at least one item");
        }

        WebTarget target = client.target(decryptionUrl);

        try (Response response = asBearer(target.request(MediaType.APPLICATION_JSON_TYPE))
                .post(Entity.json(request))) {

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(new GenericType<List<PlaintextResponse>>() {
                });
            }

            String errorBody = response.readEntity(String.class);
            throw new RuntimeException(
                    "Error " + response.getStatus() + " calling decryption service: " + errorBody
            );
        }
    }

    private Builder asBearer(Builder builder) {
        return builder.header("Authorization", "Bearer " + secret);
    }

    // Utility to decode Base64 plaintext to string
    public static String decodeBase64ToString(String base64) throws Exception {
        byte[] decoded = DatatypeConverter.parseBase64Binary(base64);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private boolean validateJNDIProperty(String value) {
        return value == null || value.startsWith("${env.");
    }

}
