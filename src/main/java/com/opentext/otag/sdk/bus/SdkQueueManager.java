/**
 * Copyright © 2018 Open Text.  All Rights Reserved.
 */
package com.opentext.otag.sdk.bus;

import java.util.Map;
import java.util.concurrent.*;

import static java.lang.String.format;

public class SdkQueueManager {

    private static final int GATEWAY_QUEUE_CAPACITY = 100;
    private static final int SERVICE_QUEUE_CAPACITY = 10;

    private static final BlockingQueue<SdkQueueEvent> GATEWAY_QUEUE = new LinkedBlockingDeque<>(GATEWAY_QUEUE_CAPACITY);

    private static final Map<String, BlockingQueue<SdkQueueEvent>> SERVICES_QUEUES = new ConcurrentHashMap<>();

    public static void registerApp(String appName) {
        if (!SERVICES_QUEUES.containsKey(appName)) {
            SERVICES_QUEUES.put(appName, new LinkedBlockingQueue<>(SERVICE_QUEUE_CAPACITY));
        }
    }

    public static void retireAppQueue(String appName) {
        SERVICES_QUEUES.remove(appName);
    }

    public static void sendEventToGateway(SdkQueueEvent toSend) {
        try {
            GATEWAY_QUEUE.put(toSend);
        } catch (InterruptedException e) {
            throw new RuntimeException(format("We failed to add event %s to the Gateway Send queue", toSend));
        }
    }

    public static void sendEventToService(String serviceName, SdkQueueEvent toSend) {
        if (SERVICES_QUEUES.containsKey(serviceName)) {
            try {
                SERVICES_QUEUES.get(serviceName).put(toSend);
            } catch (InterruptedException e) {
                throw new RuntimeException(format(
                        "We failed to add event %s to the %s service queue", toSend, serviceName));

            }
        }
    }

}
