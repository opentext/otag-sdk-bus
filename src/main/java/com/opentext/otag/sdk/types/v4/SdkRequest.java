package com.opentext.otag.sdk.types.v4;

import com.opentext.otag.sdk.bus.SdkEventKeys;

import java.util.Objects;

/**
 * A directed SDK request. The sender and receiver are expected to know what type is
 * involved in the exchange.
 *
 * @param <T> request body type
 */
public class SdkRequest<T> {

    /**
     * The destination path, this informs the Gateway which method should be called. See the {@link SdkEventKeys}
     * for all available keys.
     */
    private String endpointId;

    private T requestBody;

    public SdkRequest(String endpointId) {
        this.endpointId = endpointId;
    }

    public SdkRequest(String endpointId, T requestBody) {
        this.endpointId = endpointId;
        this.requestBody = requestBody;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public T getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(T requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SdkRequest<?> that = (SdkRequest<?>) o;
        return Objects.equals(endpointId, that.endpointId) &&
                Objects.equals(requestBody, that.requestBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointId, requestBody);
    }

    @Override
    public String toString() {
        return "SdkRequest{" +
                "endpointId='" + endpointId + '\'' +
                ", requestBody=" + requestBody +
                '}';
    }
}
