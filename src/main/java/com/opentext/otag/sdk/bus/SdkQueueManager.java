package com.opentext.otag.sdk.bus;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Central SDK event queue manager, static <strong>container-wide</strong> queues are made available
 * through this class's methods.
 */
public class SdkQueueManager {

    // singleton aids unit testing
    static SdkQueueManager instance = new SdkQueueManager();

    // configurable?
    private static final int GATEWAY_QUEUE_CAPACITY = 100;
    private static final int SERVICE_QUEUE_CAPACITY = 20;

    /**
     * All events issued from the SDK clients (AppWorks services) are to be placed on this queue.
     */
    BlockingQueue<SdkQueueEvent> GATEWAY_QUEUE = new LinkedBlockingDeque<>(GATEWAY_QUEUE_CAPACITY);

    /**
     * The AppWorks Gateway sends responses and command events to SDK clients using this queue.
     */
    Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> SERVICES_QUEUES = new ConcurrentHashMap<>();

    /**
     * The AppWorks Gateway sends replies to the service agents that wrap AppWorks services using this queue,
     * in response to SDK calls made by the agent.
     */
    Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> SERVICES_AGENT_QUEUES = new ConcurrentHashMap<>();

    /**
     * The queue the Gateway uses to issue commands to the service. Such as enable/disable, settings updates.
     */
    Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> SERVICE_COMMAND_QUEUES = new ConcurrentHashMap<>();

    /**
     * Number of attempts to put a single message on a single queue before giving up
     */
    static final int MAX_ENQUEUE_ATTEMPTS = 200;

    boolean stop = false;

    private SdkQueueManager() {
    }

    public static BlockingQueue<SdkQueueEvent> registerService(String serviceName, String persistenceContext) {
        instance.ensureServiceCommandQueue(serviceName, persistenceContext, false);
        return instance.ensureServiceQueue(serviceName, persistenceContext, false);
    }

    public static void retireService(String serviceName, String persistenceContext) {
        instance.SERVICES_QUEUES.remove(new SdkQueueEventId(serviceName, persistenceContext));
        instance.SERVICES_AGENT_QUEUES.remove(new SdkQueueEventId(serviceName, persistenceContext));
        instance.SERVICE_COMMAND_QUEUES.remove(new SdkQueueEventId(serviceName, persistenceContext));
    }

    public static void shutdown() {
        instance.stop = true;
    }

    public static boolean isShutdown() {
        return instance.stop;
    }

    /**
     * Send a request to the Gateway.
     *
     * @param toSend event to send
     */
    public static void sendEventToGateway(SdkQueueEvent toSend) {
        SdkEventBusLog.info("Sending event to Gateway - " + toSend);
        instance.put(instance.GATEWAY_QUEUE, "OTAG Q", toSend);
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
        instance.ensureServiceCommandQueue(serviceName, persistenceContext, true);
        instance.put(instance.SERVICE_COMMAND_QUEUES.get(new SdkQueueEventId(serviceName, persistenceContext)),
                "COMMAND Q", toSend);
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
        instance.ensureServiceQueue(serviceName, persistenceContext, true);
        instance.put(instance.SERVICES_QUEUES.get(new SdkQueueEventId(serviceName, persistenceContext)),
                "SERVICE RESPONSE Q", toSend);
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
        instance.ensureServiceAgentQueue(serviceName, persistenceContext, true);
        instance.put(instance.SERVICES_AGENT_QUEUES.get(new SdkQueueEventId(serviceName, persistenceContext)),
                "SERVICE AGENT Q", toSend);
    }

    public static BlockingQueue<SdkQueueEvent> gatewayQueue() {
        return instance.GATEWAY_QUEUE;
    }

    public static BlockingQueue<SdkQueueEvent> getServiceQueue(String serviceName, String persistenceUnit) {
        return instance.ensureServiceQueue(serviceName, persistenceUnit, true);
    }

    public static BlockingQueue<SdkQueueEvent> getServiceAgentQueue(String serviceName, String persistenceUnit) {
        return instance.ensureServiceAgentQueue(serviceName, persistenceUnit, true);
    }

    public static BlockingQueue<SdkQueueEvent> getServiceCommandQueue(String serviceName, String persistenceUnit) {
        return instance.ensureServiceCommandQueue(serviceName, persistenceUnit, true);
    }

    void put(BlockingQueue<SdkQueueEvent> queue, String qName, SdkQueueEvent toSend) {

        SdkEventBusLog.info("Received event to enqueue with id " + toSend.getSdkEventIdentifier() + " on " + qName +
                " at: " + new Date());

        boolean haveEnqueued = false;
        int attempts = 0;
        while (!haveEnqueued && !instance.stop && (attempts++ < MAX_ENQUEUE_ATTEMPTS)) try {
            queue.put(toSend);
            haveEnqueued = true;
        } catch (InterruptedException e) {
            if (attempts > 1) {
                try {
                    Thread.sleep(attempts + 10);
                } catch (InterruptedException ignore) {
                }
            }
            // Acknowledge that we caught the interrupt, but we know we're still in interrupted
            // state.  However, we'll ignore for now since we don't want to miss enqueuing the event.
            Thread.currentThread().interrupt();
        }
        if (haveEnqueued && attempts > 1)
            SdkEventBusLog.info("There were " + attempts + " attempts to enqueue");

        SdkEventBusLog.info((haveEnqueued ? "Completed" : "Did not complete") + " enqueue");
    }

    BlockingQueue<SdkQueueEvent> ensureServiceQueue(String serviceName, String persistenceUnit, boolean warnIfMissing) {
        SdkQueueEventId key = new SdkQueueEventId(serviceName, persistenceUnit);

        if (!instance.SERVICES_QUEUES.containsKey(key)) {
            if (warnIfMissing) {
                SdkEventBusLog.info("We attempted to send an SDK event to an app without a queue, " +
                        "adding a new queue for service " + serviceName);
            }
            instance.SERVICES_QUEUES.put(key, new LinkedBlockingQueue<>(SERVICE_QUEUE_CAPACITY));
        }

        return instance.SERVICES_QUEUES.get(key);
    }

    BlockingQueue<SdkQueueEvent> ensureServiceAgentQueue(String serviceName,
                                                         String persistenceUnit,
                                                         boolean warnIfMissing) {
        SdkQueueEventId key = new SdkQueueEventId(serviceName, persistenceUnit);

        if (!instance.SERVICES_AGENT_QUEUES.containsKey(key)) {
            if (warnIfMissing) {
                SdkEventBusLog.info("We attempted to send an SDK event to an service agent without a queue, " +
                        "adding a new queue for service agent " + serviceName);
            }
            instance.SERVICES_AGENT_QUEUES.put(key, new LinkedBlockingQueue<>(SERVICE_QUEUE_CAPACITY));
        }

        return instance.SERVICES_AGENT_QUEUES.get(key);
    }

    BlockingQueue<SdkQueueEvent> ensureServiceCommandQueue(String serviceName,
                                                           String persistenceUnit,
                                                           boolean warnIfMissing) {
        SdkQueueEventId key = new SdkQueueEventId(serviceName, persistenceUnit);

        if (!instance.SERVICE_COMMAND_QUEUES.containsKey(key)) {
            if (warnIfMissing) {
                SdkEventBusLog.info("We attempted to send an SDK event to an app without a queue, " +
                        "adding a new queue for service " + serviceName);
            }
            instance.SERVICE_COMMAND_QUEUES.put(key, new LinkedBlockingQueue<>(SERVICE_QUEUE_CAPACITY));
        }

        return instance.SERVICE_COMMAND_QUEUES.get(key);
    }

    public static BlockingQueue<SdkQueueEvent> getGatewayQueue() {
        return instance.GATEWAY_QUEUE;
    }

    public static Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> getServicesQueues() {
        return instance.SERVICES_QUEUES;
    }

    public static Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> getServicesAgentQueues() {
        return instance.SERVICES_AGENT_QUEUES;
    }

    public static Map<SdkQueueEventId, BlockingQueue<SdkQueueEvent>> getServiceCommandQueues() {
        return instance.SERVICE_COMMAND_QUEUES;
    }
}
