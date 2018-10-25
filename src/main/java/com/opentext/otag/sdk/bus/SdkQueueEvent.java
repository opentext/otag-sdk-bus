/**
 * Copyright © 2018 Open Text.  All Rights Reserved.
 */
package com.opentext.otag.sdk.bus;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * A serializable class that can wrap SDK request, response and command content so that it can be
 * queued for delivery to some remote consumer. In our case services using the SDK and the supporting
 * AppWorks Gateway may be event producers and consumers depending on the part of the SDK being used.
 */
public class SdkQueueEvent {

    public enum Type {
        // a request for some data, or for a recipient to perform some action that the sender expects a response for
        request,
        // a direct response to a request
        response,
        // an event that instruct the recipient to do something, no response required
        command,
        // an instruction to a managed queues consumers to shut down
        terminate
    }

    /**
     * Unique identifier for an event,
     */
    private String sdkEventIdentifier;

    /**
     * What is the purpose of this event.
     */
    private Type sdkEventType;

    /**
     * We need to deserialize this message
     */
    private String fullyQualifiedClassName;

    /**
     * The actual message as a JSON string.
     */
    private String sdkEventBody;

    /**
     * The type of collection contained in the event body (optional). For example, there may be a List of
     * items in the event body.
     */
    private String collectionType;

    private final Date timestamp;

    public SdkQueueEvent() {
        timestamp = new Date();
    }

    public SdkQueueEvent(Class<?> sdkBodyClazz,
                         String sdkEventBody,
                         Type sdkEventType) {
        this();
        Objects.requireNonNull(sdkEventBody, "A message body is required");
        Objects.requireNonNull(sdkBodyClazz, "The message body class is required");
        Objects.requireNonNull(sdkEventType, "A message type is required");

        sdkEventIdentifier = UUID.randomUUID().toString();

        this.fullyQualifiedClassName = sdkBodyClazz.getName();
        this.sdkEventType = sdkEventType;
        this.sdkEventBody = sdkEventBody;
    }

    /**
     * A message that indicates queue consumers should cease polling.
     *
     * @return stream termination event
     */
    public static SdkQueueEvent terminate() {
        SdkQueueEvent event = new SdkQueueEvent();
        event.setSdkEventType(Type.terminate);
        return event;
    }

    public String getSdkEventIdentifier() {
        return sdkEventIdentifier;
    }

    public void setSdkEventIdentifier(String sdkEventIdentifier) {
        this.sdkEventIdentifier = sdkEventIdentifier;
    }

    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    public void setFullyQualifiedClassName(String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    public Type getSdkEventType() {
        return sdkEventType;
    }

    public void setSdkEventType(Type sdkEventType) {
        this.sdkEventType = sdkEventType;
    }

    public String getSdkEventBody() {
        return sdkEventBody;
    }

    public void setSdkEventBody(String sdkEventBody) {
        this.sdkEventBody = sdkEventBody;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public boolean commandLikeEvent() {
        return Type.command == sdkEventType;
    }

    public boolean requestLikeEvent() {
        return Type.request == sdkEventType;
    }

    public boolean responseLikeEvent() {
        return Type.response == sdkEventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SdkQueueEvent that = (SdkQueueEvent) o;
        return Objects.equals(sdkEventIdentifier, that.sdkEventIdentifier) &&
                Objects.equals(fullyQualifiedClassName, that.fullyQualifiedClassName) &&
                sdkEventType == that.sdkEventType &&
                Objects.equals(sdkEventBody, that.sdkEventBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sdkEventIdentifier, sdkEventType);
    }

    @Override
    public String toString() {
        return "SdkQueueEvent{" +
                "sdkEventIdentifier='" + sdkEventIdentifier + '\'' +
                ", sdkEventType=" + sdkEventType +
                ", fullyQualifiedClassName='" + fullyQualifiedClassName + '\'' +
                ", sdkEventBody='" + sdkEventBody + '\'' +
                ", collectionType='" + collectionType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}