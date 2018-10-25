/**
 * Copyright © 2016 Open Text.  All Rights Reserved.
 */
package com.opentext.otag.sdk.types.v3.api;

import com.opentext.otag.sdk.types.v3.SDKType;

import java.util.Objects;

/**
 * General success/failure outcome response.
 *
 * @author Rhys Evans rhyse@opentext.com
 * @version 16.2
 */
public class SDKResponse<T> extends SDKType {

    private boolean success;

    private T responseBody;

    public SDKResponse(boolean success) {
        this.success = success;
    }

    public SDKResponse(boolean success, SDKCallInfo sdkCallInfo) {
        super(sdkCallInfo);
        this.success = success;
    }

    public SDKResponse(boolean success, T responseBody) {
        this.success = success;
        this.responseBody = responseBody;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(T responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SDKResponse<?> that = (SDKResponse<?>) o;
        return success == that.success &&
                Objects.equals(responseBody, that.responseBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), success, responseBody);
    }

    @Override
    public String toString() {
        return "SDKResponse{" +
                "success=" + success +
                ", responseBody=" + responseBody +
                "} " + super.toString();
    }

}
