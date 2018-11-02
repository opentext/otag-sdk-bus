package com.opentext.otag.sdk.bus;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.String.format;

/**
 * Central SDK event queue manager, static <strong>container-wide</strong> queues are made available
 * through this classes methods.
 */
public class SdkQueueManager {
    
    // configurable?
    private static final int GATEWAY_QUEUE_CAPACITY = 100;
    private static final int SERVICE_QUEUE_CAPACITY = 20;

    /**
     * All events issued from the SDK clients (AppWorks services) are to be placed on this queue.
     */
    private static final BlockingQueue<SdkQueueEvent> GATEWAY_QUEUE = new LinkedBlockingDeque<>(GATEWAY_QUEUE_CAPACITY);

    /**
     * The AppWorks Gateway sends responses and command events to SDK clients using this queue.
     */
    private static final Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> SERVICES_QUEUES = new ConcurrentHashMap<>();

    /**
     * The AppWorks Gateway sends replies to the service agents that wrap AppWorks services using this queue,
     * in response to SDK calls made by the agent.
     */
    private static final Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> SERVICES_AGENT_QUEUES = new ConcurrentHashMap<>();

    /**
     * The queue the Gateway uses to issue commands to the service. Such as enable/disable, settings updates.
     */
    private static final Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> SERVICE_COMMAND_QUEUES = new ConcurrentHashMap<>();

    public static BlockingQueue<SdkQueueEvent> registerService(String serviceName, String persistenceContext) {
        ensureServiceCommandQueue(serviceName, persistenceContext, false);
        return ensureServiceQueue(serviceName, persistenceContext, false);
    }

    public static void retireService(String serviceName, String persistenceContext) {
        SERVICES_QUEUES.remove(new SdkQueueEventId(serviceName, persistenceContext));
        SERVICES_AGENT_QUEUES.remove(new SdkQueueEventId(serviceName, persistenceContext));
        SERVICE_COMMAND_QUEUES.remove(new SdkQueueEventId(serviceName, persistenceContext));
    }

    /**
     * Send a request to the Gateway.
     *
     * @param toSend event to send
     */
    public static void sendEventToGateway(SdkQueueEvent toSend) {
        SdkEventBusLog.info("Sending event to Gateway - " + toSend);
        try {
            GATEWAY_QUEUE.put(toSend);
            SdkEventBusLog.info("Put event with id " + toSend.getSdkEventIdentifier() +
                    " on OTAG Q at:" + new Date());
        } catch (InterruptedException e) {
            throw new RuntimeException(format("We failed to add event %s to the Gateway Send queue", toSend));
        }
    }

    /**
     * Send a command to  a service.
     *
     * @param serviceName        service name
     * @param persistenceContext tenant context name
     * @param toSend             event to send on queue
     */
    public static void sendCommandToService(String serviceName,
                                            String persistenceContext,
                                            SdkQueueEvent toSend) {
        ensureServiceCommandQueue(serviceName, persistenceContext, true);

        try {
            SERVICE_COMMAND_QUEUES.get(new SdkQueueEventId(serviceName, persistenceContext)).put(toSend);
            SdkEventBusLog.info("Put event with id " + toSend.getSdkEventIdentifier() +
                    " on COMMAND Q at:" + new Date());
        } catch (InterruptedException e) {
            throw new RuntimeException(format(
                    "We failed to add event %s to the %s service queue", toSend, serviceName));
        }
    }

    /**
     * Send an SdkQueueEvent to a named tenant service queue.
     *
     * @param serviceName        service name
     * @param persistenceContext tenant context name
     * @param toSend             event to send on queue
     */
    public static void sendEventToService(String serviceName,
                                          String persistenceContext,
                                          SdkQueueEvent toSend) {
        ensureServiceQueue(serviceName, persistenceContext, true);

        try {
            SERVICES_QUEUES.get(new SdkQueueEventId(serviceName, persistenceContext)).put(toSend);
            SdkEventBusLog.info("Put event with id " + toSend.getSdkEventIdentifier() +
                    " on SERVICE RESPONSE Q at:" + new Date());

        } catch (InterruptedException e) {
            throw new RuntimeException(format(
                    "We failed to add event %s to the %s service queue", toSend, serviceName));
        }
    }

    /**
     * Send an SdkQueueEvent to a named tenant service queue.
     *
     * @param serviceName        service name
     * @param persistenceContext tenant context name
     * @param toSend             event to send on queue
     */
    public static void sendEventToAgent(String serviceName,
                                        String persistenceContext,
                                        SdkQueueEvent toSend) {
        ensureServiceAgentQueue(serviceName, persistenceContext, true);

        try {
            SERVICES_AGENT_QUEUES.get(new SdkQueueEventId(serviceName, persistenceContext)).put(toSend);

            SdkEventBusLog.info("Put event with id " + toSend.getSdkEventIdentifier() + " on SERVICE AGENT Q at:" + new Date());

        } catch (InterruptedException e) {
            throw new RuntimeException(format(
                    "We failed to add event %s to the %s service queue", toSend, serviceName));
        }
    }

    public static BlockingQueue<SdkQueueEvent> gatewayQueue() {
        return GATEWAY_QUEUE;
    }

    public static BlockingQueue<SdkQueueEvent> getServiceQueue(String serviceName, String persistenceUnit) {
        return ensureServiceQueue(serviceName, persistenceUnit, true);
    }

    public static BlockingQueue<SdkQueueEvent> getServiceAgentQueue(String serviceName, String persistenceUnit) {
        return ensureServiceAgentQueue(serviceName, persistenceUnit, true);
    }

    public static BlockingQueue<SdkQueueEvent> getServiceCommandQueue(String serviceName, String persistenceUnit) {
        return ensureServiceCommandQueue(serviceName, persistenceUnit, true);
    }

    private static synchronized BlockingQueue<SdkQueueEvent> ensureServiceQueue(String serviceName,
                                                                                String persistenceUnit,
                                                                                boolean warnIfMissing) {
        SdkQueueEventId key = new SdkQueueEventId(serviceName, persistenceUnit);

        if (!SERVICES_QUEUES.containsKey(key)) {
            if (warnIfMissing) {
                SdkEventBusLog.info("The Gateway attempted to send an SDK event to a service without an event queue, " +
                        "adding a new queue for service " + serviceName);
            }
            SERVICES_QUEUES.put(key, new LinkedBlockingQueue<>(SERVICE_QUEUE_CAPACITY));
        }

        return SERVICES_QUEUES.get(key);
    }

    private static synchronized BlockingQueue<SdkQueueEvent> ensureServiceAgentQueue(String serviceName,
                                                                        String persistenceUnit,
                                                                        boolean warnIfMissing) {
        SdkQueueEventId key = new SdkQueueEventId(serviceName, persistenceUnit);

        if (!SERVICES_AGENT_QUEUES.containsKey(key)) {
            if (warnIfMissing) {
                SdkEventBusLog.info("We attempted to send an SDK event to a service agent without an event queue, " +
                        "adding a new queue for service agent " + serviceName);
            }
            SERVICES_AGENT_QUEUES.put(key, new LinkedBlockingQueue<>(SERVICE_QUEUE_CAPACITY));
        }

        return SERVICES_AGENT_QUEUES.get(key);
    }

    private static synchronized BlockingQueue<SdkQueueEvent> ensureServiceCommandQueue(String serviceName,
                                                                          String persistenceUnit,
                                                                          boolean warnIfMissing) {
        SdkQueueEventId key = new SdkQueueEventId(serviceName, persistenceUnit);

        if (!SERVICE_COMMAND_QUEUES.containsKey(key)) {
            if (warnIfMissing) {
                SdkEventBusLog.info("The Gateway attempted to send an SDK event to a service without an event queue, " +
                        "adding a new queue for service " + serviceName);
            }
            SERVICE_COMMAND_QUEUES.put(key, new LinkedBlockingQueue<>(SERVICE_QUEUE_CAPACITY));
        }

        return SERVICE_COMMAND_QUEUES.get(key);
    }

    public static BlockingQueue<SdkQueueEvent> getGatewayQueue() {
        return GATEWAY_QUEUE;
    }

    public static Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> getServicesQueues() {
        return SERVICES_QUEUES;
    }

    public static Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> getServicesAgentQueues() {
        return SERVICES_AGENT_QUEUES;
    }

    public static Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> getServiceCommandQueues() {
        return SERVICE_COMMAND_QUEUES;
    }
}
