package io.arbitrix.core.common.http;

/**
 * Interface for handling HTTP headers in Feign requests
 */
public interface HttpHeaderHandler {

    /**
     * Determine if a header should be included in the request
     *
     * @param headerKey the header key
     * @return true if the header should be included, false otherwise
     */
    boolean shouldIncludeHeader(String headerKey);
}
