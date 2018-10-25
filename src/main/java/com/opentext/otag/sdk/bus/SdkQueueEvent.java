package com.opentext.otag.sdk.bus;

import com.opentext.otag.sdk.types.v3.OtagServiceEvent;
import com.opentext.otag.sdk.types.v3.SDKType;
import com.opentext.otag.sdk.types.v3.api.SDKResponse;
import com.opentext.otag.sdk.types.v3.message.*;
import com.opentext.otag.sdk.types.v4.SdkRequest;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.of;

public class SdkQueueEvent implements Serializable {

    public enum Type {
        wakeup,
        // a request for some data, or for a recipient to perform some action that the sender expects a response for
        request,
        // a direct response to a request
        response,
        // an error response
        error,
        // an event that instruct the recipient to do something, no response required
        command,
        // an instruction to a managed queues consumers to shut down
        terminate
    }

    public enum ClientType { service, agent }

    /**
     * Unique identifier for an event,
     */
    private String sdkEventIdentifier;

    private String serviceName;

    /**
     * The client type tells the Gateway which queue to respond on. The agent and service have separate
     * response queues.
     */
    private ClientType clientType;

    /**
     * A specific persistence context derived from the web.xml for the service. The Gateway
     * may be deployed in a multi-tenant fashion, this is the identifier for a tenant.
     */
    private String persistenceContext;

    /**
     * What is the purpose of this event.
     */
    private Type sdkEventType;

    private final Date timestamp;

    private AccessChangeMessage accessChangeMessage;
    private AuthRequestMessage authRequestMessage;
    private EimConnectorUpdateMessage eimConnectorUpdateMessage;
    private LifecycleChangeMessage lifecycleChangeMessage;
    private SettingsChangeMessage settingsChangeMessage;

    /**
     * Sdk object being passed in the message.
     */
    private SDKType sdkType;

    private SdkRequest sdkRequest;

    /**
     * An SDK response.
     */
    private SDKResponse sdkResponse;

    public SdkQueueEvent() {
        sdkEventIdentifier = UUID.randomUUID().toString();
        timestamp = new Date();
        clientType = ClientType.service;
    }

    public SdkQueueEvent(String serviceName, String persistenceContext) {
        this();
        this.serviceName = serviceName;
        this.persistenceContext = persistenceContext;
    }

    public SdkQueueEvent(String sdkEventIdentifier, String serviceName, String persistenceContext) {
        this.sdkEventIdentifier = sdkEventIdentifier;
        this.serviceName = serviceName;
        this.persistenceContext = persistenceContext;
        timestamp = new Date();
    }

    /**
     * This request is used to start a blocking queues thread.
     *
     * @return start taking from the queue event
     */
    public static SdkQueueEvent start() {
        SdkQueueEvent startEvent = new SdkQueueEvent();
        startEvent.setSdkEventType(Type.wakeup);
        return startEvent;
    }

    public static SdkQueueEvent request(SdkRequest<?> request,
                                        String serviceName,
                                        String persistenceContext) {
        SdkQueueEvent event = new SdkQueueEvent(serviceName, persistenceContext);
        event.setSdkRequest(request);
        event.setSdkEventType(Type.request);
        return event;
    }

    public static SdkQueueEvent okResponse(SdkQueueEvent request) {
        return response(new SDKResponse(true), request);
    }

    public static SdkQueueEvent response(SDKResponse sdkResponse,
                                         SdkQueueEvent request) {
        SdkQueueEvent event = new SdkQueueEvent(request.getSdkEventIdentifier(),
                request.getServiceName(), request.getPersistenceContext());
        event.setSdkResponse(sdkResponse);
        event.setSdkEventType(Type.response);
        event.setClientType(request.getClientType());
        return event;
    }

    public static SdkQueueEvent command(OtagMessage commandObject, String serviceName, String persistenceContext) {
        SdkQueueEvent event = new SdkQueueEvent(serviceName, persistenceContext);
        event.setSdkEventType(Type.command);

        return event;
    }

    public static SdkQueueEvent error(SDKResponse sdkResponse, String sdkEventIdentifier,
                                      String serviceName, String persistenceContext) {
        SdkQueueEvent event = new SdkQueueEvent(serviceName, persistenceContext);
        event.setSdkEventIdentifier(sdkEventIdentifier);
        event.setSdkResponse(sdkResponse);
        event.setSdkEventType(Type.error);
        return event;
    }

    public static SdkQueueEvent error(SDKResponse sdkResponse, SdkQueueEvent request) {
        return error(sdkResponse, request.getSdkEventIdentifier(),
                request.getServiceName(), request.getPersistenceContext());
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

    public static <T> Optional<T> extractBodyFromRequest(SdkQueueEvent sdkEvent,
                                                         Class<T> bodyType) {
        SdkRequest sdkRequest = sdkEvent.getSdkRequest();
        if (sdkRequest != null) {
            Object requestBody = sdkRequest.getRequestBody();
            if (bodyType.isInstance(requestBody)) {
                return of(bodyType.cast(requestBody));
            }
        }

        return Optional.empty();
    }

    public static <T> Optional<T> extractBodyFromResponse(SdkQueueEvent sdkEvent,
                                                          Class<T> bodyType) {
        SDKResponse sdkResponse = sdkEvent.getSdkResponse();
        if (sdkResponse != null) {
            Object requestBody = sdkResponse.getResponseBody();
            if (bodyType.isInstance(requestBody)) {
                return of(bodyType.cast(requestBody));
            }
        }

        return Optional.empty();
    }

    public String getSdkEventIdentifier() {
        return sdkEventIdentifier;
    }

    public void setSdkEventIdentifier(String sdkEventIdentifier) {
        this.sdkEventIdentifier = sdkEventIdentifier;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Type getSdkEventType() {
        return sdkEventType;
    }

    public void setSdkEventType(Type sdkEventType) {
        this.sdkEventType = sdkEventType;
    }

    public SDKType getSdkType() {
        return sdkType;
    }

    public void setSdkType(SDKType sdkType) {
        this.sdkType = sdkType;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public SDKResponse getSdkResponse() {
        return sdkResponse;
    }

    public void setSdkResponse(SDKResponse sdkResponse) {
        this.sdkResponse = sdkResponse;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public SdkRequest getSdkRequest() {
        return sdkRequest;
    }

    public void setSdkRequest(SdkRequest sdkRequest) {
        this.sdkRequest = sdkRequest;
    }

    public AccessChangeMessage getAccessChangeMessage() {
        return accessChangeMessage;
    }

    public void setAccessChangeMessage(AccessChangeMessage accessChangeMessage) {
        this.accessChangeMessage = accessChangeMessage;
    }

    public AuthRequestMessage getAuthRequestMessage() {
        return authRequestMessage;
    }

    public void setAuthRequestMessage(AuthRequestMessage authRequestMessage) {
        this.authRequestMessage = authRequestMessage;
    }

    public EimConnectorUpdateMessage getEimConnectorUpdateMessage() {
        return eimConnectorUpdateMessage;
    }

    public void setEimConnectorUpdateMessage(EimConnectorUpdateMessage eimConnectorUpdateMessage) {
        this.eimConnectorUpdateMessage = eimConnectorUpdateMessage;
    }

    public LifecycleChangeMessage getLifecycleChangeMessage() {
        return lifecycleChangeMessage;
    }

    public void setLifecycleChangeMessage(LifecycleChangeMessage lifecycleChangeMessage) {
        this.lifecycleChangeMessage = lifecycleChangeMessage;
    }

    public SettingsChangeMessage getSettingsChangeMessage() {
        return settingsChangeMessage;
    }

    public void setSettingsChangeMessage(SettingsChangeMessage settingsChangeMessage) {
        this.settingsChangeMessage = settingsChangeMessage;
    }

    /**
     * Get the destination endpoint id, if defined.
     *
     * @return request endpoint id
     */
    public String getDestination() {
        return sdkRequest == null ? "" : sdkRequest.getEndpointId() == null ? "" : sdkRequest.getEndpointId();
    }

    /**
     * Services can be deployed for more than one tenant so we need to specify the persistence context too.
     * It can be read from the services web.xml file.
     *
     * @param serviceName        service name
     * @param persistenceContext persistence context name
     * @return true if this event is for this service instance
     */
    public boolean isForService(String serviceName, String persistenceContext) {
        return Objects.equals(this.serviceName, serviceName) &&
                Objects.equals(this.persistenceContext, persistenceContext);
    }

    public OtagServiceEvent getOtagServiceEvent() {
        if (accessChangeMessage != null) {
            return accessChangeMessage.getEvent();
        }
        if (authRequestMessage != null) {
            return authRequestMessage.getEvent();
        }
        if (eimConnectorUpdateMessage != null) {
            return eimConnectorUpdateMessage.getEvent();
        }
        if (lifecycleChangeMessage != null) {
            return lifecycleChangeMessage.getEvent();
        }
        if (settingsChangeMessage != null) {
            return settingsChangeMessage.getEvent();
        }

        return null;

    }

    public boolean isRequest() {
        return Type.request == sdkEventType && sdkRequest != null && sdkRequest.getEndpointId() != null;
    }

    public boolean isCommand() {
        return Type.command == sdkEventType && getOtagServiceEvent() != null;
    }

    public boolean isError() {
        return Type.error == sdkEventType;
    }

    public boolean isTerminationCommand() {
        return Type.terminate == sdkEventType;
    }

    public boolean isFromService() {
        return ClientType.service == clientType;
    }

    public boolean isFromAgent() {
        return ClientType.agent == clientType;
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
        SdkQueueEvent event = (SdkQueueEvent) o;
        return Objects.equals(sdkEventIdentifier, event.sdkEventIdentifier) &&
                Objects.equals(serviceName, event.serviceName) &&
                Objects.equals(persistenceContext, event.persistenceContext) &&
                sdkEventType == event.sdkEventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sdkEventIdentifier, serviceName, persistenceContext, sdkEventType);
    }

    @Override
    public String toString() {
        return "SdkQueueEvent{" +
                "sdkEventIdentifier='" + sdkEventIdentifier + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", clientType=" + clientType +
                ", persistenceContext='" + persistenceContext + '\'' +
                ", sdkEventType=" + sdkEventType +
                ", timestamp=" + timestamp +
                ", accessChangeMessage=" + accessChangeMessage +
                ", authRequestMessage=" + authRequestMessage +
                ", eimConnectorUpdateMessage=" + eimConnectorUpdateMessage +
                ", lifecycleChangeMessage=" + lifecycleChangeMessage +
                ", settingsChangeMessage=" + settingsChangeMessage +
                ", sdkType=" + sdkType +
                ", sdkRequest=" + sdkRequest +
                ", sdkResponse=" + sdkResponse +
                '}';
    }
}