package org.openstack4j.connectors.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.core.transport.ExecutionOptions;
import org.openstack4j.core.transport.HttpEntityHandler;
import org.openstack4j.core.transport.HttpResponse;
import org.openstack4j.core.transport.ObjectMapperSingleton;
import org.openstack4j.openstack.logging.Logger;
import org.openstack4j.openstack.logging.LoggerFactory;

public class HttpResponseImpl implements HttpResponse {

    private static final Logger LOG = LoggerFactory.getLogger(HttpResponseImpl.class);

    private Map<String, List<String>> headers;
    private int responseCode;
    private String responseMessage;
    private String body;

    private HttpResponseImpl(Map<String, List<String>> headers,
            int responseCode, String responseMessage, String body) {
        this.headers = headers;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.body = body;
    }

    /**
     * Wrap the given Response
     *
     * @param headers
     * @param responseCode
     * @param responseMessage
     * @return the HttpResponse
     */
    public static HttpResponseImpl wrap(Map<String, List<String>> headers,
            int responseCode, String responseMessage, String body) {
        return new HttpResponseImpl(headers, responseCode, responseMessage, body);
    }

    /**
     * Gets the entity and Maps any errors which will result in a
     * ResponseException
     *
     * @param <T> the generic type
     * @param returnType the return type
     * @return the entity
     */
    public <T> T getEntity(Class<T> returnType) {
        return getEntity(returnType, null);
    }

    /**
     * Gets the entity and Maps any errors which will result in a
     * ResponseException
     *
     * @param <T> the generic type
     * @param returnType the return type
     * @param options the execution options
     * @return the entity
     */
    @Override
    public <T> T getEntity(Class<T> returnType, ExecutionOptions<T> options) {
        return HttpEntityHandler.handle(this, returnType, options, Boolean.TRUE);
    }

    /**
     * Gets the status from the previous Request
     *
     * @return the status code
     */
    public int getStatus() {
        return responseCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusMessage() {
        return responseMessage;
    }

    /**
     * @return the input stream
     */
    public InputStream getInputStream() {
        return null;
    }

    /**
     * Returns a Header value from the specified name key
     *
     * @param name the name of the header to query for
     * @return the header as a String or null if not found
     */
    public String header(String name) {
        List<String> values = headers.get(name);
        if (values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }

    /**
     * @return the a Map of Header Name to Header Value
     */
    public Map<String, String> headers() {
        Map<String, String> retHeaders = new HashMap<String, String>();

        Set<String> keys = headers.keySet();

        for (String key : keys) {
            List<String> values = headers.get(key);
            for (String value : values) {
                retHeaders.put(key, value);
            }
        }

        return retHeaders;
    }

    @Override
    public <T> T readEntity(Class<T> typeToReadAs) {
        try {
            return ObjectMapperSingleton.getContext(typeToReadAs).reader(typeToReadAs).readValue(body);
        } catch (Exception e) {
            LOG.error(e, e.getMessage());
            throw new ClientResponseException(e.getMessage(), 0, e);
        }
    }
}
