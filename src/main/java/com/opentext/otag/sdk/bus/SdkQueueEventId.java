package com.opentext.otag.sdk.bus;

import java.util.Objects;

public class SdkQueueEventId {

    private String serviceName;
    private String persistenceContext;

    public SdkQueueEventId() {
    }

    public SdkQueueEventId(String serviceName, String persistenceContext) {
        this.serviceName = serviceName;
        this.persistenceContext = persistenceContext;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPersistenceContext() {
        return persistenceContext;
    }

    public void setPersistenceContext(String persistenceContext) {
        this.persistenceContext = persistenceContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SdkQueueEventId that = (SdkQueueEventId) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(persistenceContext, that.persistenceContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, persistenceContext);
    }

    @Override
    public String toString() {
        return "SdkQueueEventId{" +
                "serviceName='" + serviceName + '\'' +
                ", persistenceContext='" + persistenceContext + '\'' +
                '}';
    }
}
