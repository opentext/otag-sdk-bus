package com.opentext.otag.sdk.bus;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import static com.opentext.otag.sdk.bus.SdkQueueManager.gatewayQueue;
import static com.opentext.otag.sdk.bus.SdkQueueManager.getServiceQueue;

public class SdkBusTester {

    private static final String TEST_SERVICE = "testService";
    private static final String PERS_CTX = "persCtx";

    protected static final BlockingQueue<SdkQueueEvent> SERVICE_QUEUE = getServiceQueue(TEST_SERVICE, PERS_CTX);
    protected static final BlockingQueue<SdkQueueEvent> GATEWAY_QUEUE = gatewayQueue();

    // each service instantiates one of these to listen for responses to SDK events they sent to the Gateway
    protected SdkQueueCallbackManager callbackManager;

    private CountDownLatch countDownLatch;

    @Before
    public void beforeEach() {
        // refresh the callbacks each test
        callbackManager = new SdkQueueCallbackManager(TEST_SERVICE, PERS_CTX, false);
    }

    @After
    public void afterEach() {
        callbackManager.stop();
    }

}
