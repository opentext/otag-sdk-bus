/**
 * Copyright © 2016 Open Text.  All Rights Reserved.
 */
package com.opentext.otag.sdk.types.v3.message;

import com.opentext.otag.sdk.types.v3.OtagServiceEvent;
import com.opentext.otag.sdk.types.v3.client.ClientRepresentation;
import com.opentext.otag.sdk.util.ForwardHeaders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * ApWorks Gateway message used to request authentication from an AppWorks
 * service that implements an authentication request handler via
 * the AppWorks service development kit.
 *
 * @author Rhys Evans rhyse@opentext.com
 * @version 16.2
 */
public class AuthRequestMessage extends OtagMessageImpl {

    public static final HashSet<OtagServiceEvent> AUTH_EVENTS =
            new HashSet<>(Arrays.asList(OtagServiceEvent.AUTH_REQUEST,
                    OtagServiceEvent.DECORATE_AUTH_RESPONSE));

    /**
     * The unique identifier of the handler we wish to fulfil the request.
     */
    private String handlerName;

    private String username;
    private String password;
    private String authToken;
    private ForwardHeaders forwardHeaders;
    private ClientRepresentation clientData;

    public AuthRequestMessage() {
    }

    public AuthRequestMessage(OtagServiceEvent event) {
        super(event);
    }

    public static AuthRequestMessage authByCredsMsg(String handlerName,
                                                    String username,
                                                    String password,
                                                    ForwardHeaders forwardHeaders,
                                                    ClientRepresentation clientData) {
        AuthRequestMessage message = new AuthRequestMessage(OtagServiceEvent.AUTH_REQUEST);
        message.username = username;
        message.handlerName = handlerName;
        message.password = password;
        message.forwardHeaders = forwardHeaders;
        message.clientData = clientData;
        return message;
    }

    public static AuthRequestMessage authByTokenMsg(String handlerName,
                                                    String authToken,
                                                    ForwardHeaders forwardHeaders,
                                                    ClientRepresentation clientData) {
        AuthRequestMessage message = new AuthRequestMessage(OtagServiceEvent.AUTH_REQUEST);
        message.handlerName = handlerName;
        message.authToken = authToken;
        message.forwardHeaders = forwardHeaders;
        message.clientData = clientData;
        return message;
    }

    public static AuthRequestMessage authByOtdsMsg(String handlerName,
                                                   String otdsTicket,
                                                   ForwardHeaders forwardHeaders,
                                                   ClientRepresentation clientData) {
        AuthRequestMessage message = new AuthRequestMessage(OtagServiceEvent.AUTH_REQUEST);
        message.handlerName = handlerName;
        message.authToken = otdsTicket;
        message.forwardHeaders = forwardHeaders;
        message.clientData = clientData;
        return message;
    }

    public static AuthRequestMessage decorateByCreds(String username,
                                                     String password,
                                                     ForwardHeaders forwardHeaders,
                                                     ClientRepresentation clientData) {
        AuthRequestMessage message = new AuthRequestMessage(OtagServiceEvent.DECORATE_AUTH_RESPONSE);
        message.username = username;
        message.password = password;
        message.forwardHeaders = forwardHeaders;
        message.clientData = clientData;
        return message;
    }

    public static AuthRequestMessage decorateByAuthToken(String authToken,
                                                         ForwardHeaders forwardHeaders,
                                                         ClientRepresentation clientData) {
        AuthRequestMessage message = new AuthRequestMessage(OtagServiceEvent.DECORATE_AUTH_RESPONSE);
        message.authToken = authToken;
        message.forwardHeaders = forwardHeaders;
        message.clientData = clientData;
        return message;
    }

    public boolean hasAuthToken() {
        return getAuthToken() != null;
    }

    public boolean hasCreds() {
        return getUsername() != null &&
                getPassword() != null;
    }

    @Override
    public Set<OtagServiceEvent> getSupportedTypes() {
        return AUTH_EVENTS;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthToken() {
        return authToken;
    }

    public ForwardHeaders getForwardHeaders() {
        return forwardHeaders;
    }

    public ClientRepresentation getClientData() {
        return clientData;
    }

    @Override
    public String toString() {
        return "AuthRequestMessage{" +
                "handlerName='" + handlerName + '\'' +
                ", username='" + username + '\'' +
                ", clientData=" + clientData +
                "} " + super.toString();
    }
}
