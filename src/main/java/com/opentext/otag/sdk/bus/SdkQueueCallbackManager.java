package com.opentext.otag.sdk.bus;

import com.opentext.otag.sdk.types.v3.api.SDKCallInfo;
import com.opentext.otag.sdk.types.v3.api.error.APIException;
import com.opentext.otag.sdk.util.StringUtil;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * SDK requests are passed along a central queue, but its methods are synchronous
 * in nature. We hide this fact by managing the request/response cycle using the
 * available queues
 */
public class SdkQueueCallbackManager {
    
    private final ExecutorService sdkConsumerExecutor;
    private final Future<?> responseConsumerFuture;

    /**
     * SDK callback queues are mapped against their {@link SdkQueueEvent#sdkEventIdentifier}. Once a response
     * is received it is thrown onto the queue. The blocking queue is used by clients of this
     */
    private final Map<String, BlockingQueue<SdkQueueEvent>> CALLBACKS_QUEUES = new ConcurrentHashMap<>();

    /**
     * Is this callback manager being used by a service agent? As opposed to a general SDK client.
     */
    private final boolean isAgentManager;

    public SdkQueueCallbackManager(String serviceName,
                                   String persistenceContext,
                                   boolean isAgentManager) {
        this.isAgentManager = isAgentManager;
        sdkConsumerExecutor = Executors.newSingleThreadExecutor(r ->
                new Thread(r, "SdkResponseConsumer-" +
                        ((isAgentManager) ? "AGENT" : "SERVICE") + "-" +
                        serviceName + ":" + persistenceContext));
        responseConsumerFuture = sdkConsumerExecutor.submit(getConsumerRunnable(serviceName, persistenceContext));
    }

    public static SdkQueueCallbackManager serviceCbackManager(String serviceName,
                                                                 String persistenceContext) {
        return new SdkQueueCallbackManager(serviceName, persistenceContext, false);
    }

    public static SdkQueueCallbackManager agentCbackManager(String serviceName,
                                                                   String persistenceContext) {
        return new SdkQueueCallbackManager(serviceName, persistenceContext, true);
    }

    /**
     * Shut down the callback manager.
     */
    public void stop() {
        try {
            if (responseConsumerFuture != null) {
                responseConsumerFuture.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SdkEventBusLog.info("Failed to shut down the active queue getConsumerRunnable: " + e.getMessage());
        }

        try {
            if (sdkConsumerExecutor != null) {
                sdkConsumerExecutor.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SdkEventBusLog.info("Failed to shut down the getConsumerRunnable Executor service: " + e.getMessage());
        }
    }

    /**
     * This method will wait for a response event to come from the Gateway.
     *
     * @param eventId unique event id
     * @return the matching response event
     * @throws InterruptedException if the response for the provided id is not passed back to the SDK
     *                              bus in a reasonable amount of time
     */
    public SdkQueueEvent getResponseForEvent(String eventId) throws InterruptedException {
        // we use the BlockingQueue again here to force the async nature of waiting for
        // the Gateway to respond into a sync one
        final BlockingQueue<SdkQueueEvent> blockingQueue = new ArrayBlockingQueue<>(1);

        SdkEventBusLog.info("Placing event with id " + eventId + " in the callback queue at:" + new Date());

        CALLBACKS_QUEUES.put(eventId, blockingQueue);

        // wait for an event to be added to the queue by the consumer for a few seconds
        SdkQueueEvent returnEvent = blockingQueue.poll(30, TimeUnit.SECONDS);

        // we do not want to return null to our consumers ever
        if (returnEvent == null) {
            String errString = "SDK request for event " + eventId + "timed out awaiting a response";
            SdkEventBusLog.error(errString);
            throw new APIException(errString, new SDKCallInfo());
        }

        CALLBACKS_QUEUES.remove(eventId);
        return returnEvent;
    }

    private Callable<Object> getConsumerRunnable(String serviceName, String persistenceContext) {
        return () -> {
            SdkEventBusLog.info("Starting SDK queue callback for " + serviceName + " " +
                    (isAgentManager ? "Service Agent" : "Service"));

            BlockingQueue<SdkQueueEvent> serviceQueue = (isAgentManager) ?
                    SdkQueueManager.getServiceAgentQueue(serviceName, persistenceContext) :
                    SdkQueueManager.getServiceQueue(serviceName, persistenceContext);

            while (true) {
                String consumerName = ((isAgentManager) ? "AGENT" : "SERVICE") + " CONSUMER";
                SdkEventBusLog.info("On take loop: " + consumerName);
                SdkQueueEvent responseEvent = serviceQueue.take();
                String eventId = responseEvent.getSdkEventIdentifier();

                SdkEventBusLog.info(consumerName + ": Got event with id " + eventId + " in the callback queue at:" + new Date());
                SdkEventBusLog.info(consumerName + ": Got event " + responseEvent + " in the callback queue at:" + new Date());
                SdkEventBusLog.info(consumerName + ": Callback Queue - " + StringUtil.toListString(CALLBACKS_QUEUES.keySet()) +
                        " at:" + new Date());

                if (CALLBACKS_QUEUES.containsKey(eventId)) {
                    try {
                        BlockingQueue<SdkQueueEvent> blockingQueue = CALLBACKS_QUEUES.get(eventId);
                        SdkEventBusLog.info(consumerName + ": Passing event to callback queue for " + eventId +
                                " event - " + responseEvent + " - queue size=" + blockingQueue.size());
                        blockingQueue.add(responseEvent);
                    } catch (Exception e) {
                        SdkEventBusLog.info(consumerName + ": Call back for " + eventId + " FAILED!!!, " +
                                "removing from callbacks: " + e.getMessage() + ":" + e.toString());
                        CALLBACKS_QUEUES.remove(eventId);
                    }
                } else {
                    SdkEventBusLog.info("Response without a registered callback was received - " + responseEvent);
                }
            }

        };
    }

}
