package com.opentext.otag.sdk.bus;

import com.opentext.otag.sdk.types.v3.api.SDKResponse;
import com.opentext.otag.sdk.types.v3.management.DeploymentResult;
import com.opentext.otag.sdk.types.v3.settings.Setting;
import com.opentext.otag.sdk.types.v3.settings.SettingType;
import com.opentext.otag.sdk.types.v3.settings.Settings;
import com.opentext.otag.sdk.types.v4.SdkRequest;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.opentext.otag.sdk.bus.SdkEventKeys.SERVICE_MGMT_COMPLETE_DEPLOYMENT;
import static com.opentext.otag.sdk.bus.SdkEventKeys.SETTINGS_GET_SETTINGS;
import static java.util.Arrays.asList;

/**
 * This test intends to emulate a real Gateway deployment scenario; including a Gateway consumer and producer,
 * as well as client service and client agent.
 */
@SuppressWarnings("unchecked")
public class SdkCallResponseTest extends SdkBusTester {

    private static final Logger LOG = Logger.getLogger(SdkCallResponseTest.class.getName());

    private static final String TEST_SERVICE = "testService";
    private static final String PERS_CTX = "persCtx";

    private CountDownLatch countDownLatch;

    // Steps
    // 1. agent sends deployment complete event
    // 2. gateway responds with success event
    // 3. service sends get settings request, and waits for response via the callback manager
    // 4. gateway responds with event containing settings
    @Test
    public void runTest() throws InterruptedException {
        SdkQueueEvent deploymentCompleteEvt = SdkQueueEvent.request(
                new SdkRequest<>(SERVICE_MGMT_COMPLETE_DEPLOYMENT, new DeploymentResult(true)), TEST_SERVICE, PERS_CTX);
        countDownLatch = new CountDownLatch(4);

        Thread gatewayThread = new Thread(getGatewayRunnable());
        gatewayThread.start();

        Thread serviceThread = new Thread(getServiceRunnable(deploymentCompleteEvt));
        serviceThread.start();

        SERVICE_QUEUE.add(SdkQueueEvent.start());
        GATEWAY_QUEUE.add(deploymentCompleteEvt);

        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    private Runnable getServiceRunnable(final SdkQueueEvent deploymentCompleteEvt) {
        return () -> {
            LOG.log(Level.INFO, "SERVICE thread");

            while (!SdkQueueManager.isShutdown()) {
                try {
                    SdkQueueEvent fromOtag = SERVICE_QUEUE.take();
                    LOG.log(Level.INFO, "Take off SERVICE_QUEUE, got - " + fromOtag);

                    String deploymentCompleteEventId = deploymentCompleteEvt.getSdkEventIdentifier();

                    if (fromOtag.getSdkEventIdentifier().equals(deploymentCompleteEventId)) {
                        LOG.log(Level.INFO, "2. service agent received a DEPLOYMENT COMPLETE ACK event");
                        countDownLatch.countDown();

                        SdkQueueEvent settingsRequest = performSettingsRequest();
                        LOG.log(Level.INFO, "4. service agent received Settings via SDK");
                        SDKResponse sdkResponse = settingsRequest.getSdkResponse();
                        Settings responseBody = (Settings) sdkResponse.getResponseBody();

                        LOG.log(Level.INFO, "Got " + responseBody.getSettings().size() + " settings in response");
                        responseBody.getSettings().forEach(setting ->
                                LOG.log(Level.INFO, "Got setting - " + setting));
                        countDownLatch.countDown();
                    }

                } catch (Throwable t) {
                    // log and ignore
                    SdkEventBusLog.error("Ignoring error", t);
                }
            }

        };
    }

    private SdkQueueEvent performSettingsRequest() throws InterruptedException {
        SdkQueueEvent settingsRequest = SdkQueueEvent.request(
                new SdkRequest<>(SETTINGS_GET_SETTINGS), TEST_SERVICE, PERS_CTX);

        GATEWAY_QUEUE.put(settingsRequest);

        return callbackManager.getResponseForEvent(settingsRequest.getSdkEventIdentifier());
    }

    private Runnable getGatewayRunnable() {
        return () -> {
            LOG.log(Level.INFO, "GATEWAY thread");
            while (!SdkQueueManager.isShutdown()) {
                try {
                    SdkQueueEvent toOtag = GATEWAY_QUEUE.take();
                    LOG.log(Level.INFO, "Take off GATEWAY_QUEUE, got - " + toOtag);

                    String endpointId = toOtag.getSdkRequest().getEndpointId();
                    LOG.log(Level.INFO, "Event endpoint=" + endpointId);

                    if (SERVICE_MGMT_COMPLETE_DEPLOYMENT.equals(endpointId)) {
                        LOG.log(Level.INFO, "1. agent sent a DEPLOYMENT COMPLETE event");
                        countDownLatch.countDown();

                        SdkQueueEvent ack = new SdkQueueEvent(TEST_SERVICE, PERS_CTX);
                        ack.setSdkResponse(new SDKResponse(true));
                        ack.setSdkEventIdentifier(toOtag.getSdkEventIdentifier());

                        boolean add = SERVICE_QUEUE.add(ack);
                        LOG.log(Level.INFO, "Gateway responded with ack on SERVICE_QUEUE- succeeded = " + add);
                    } else if (SETTINGS_GET_SETTINGS.equals(endpointId)) {
                        LOG.log(Level.INFO, "3. service sent a GET SETTINGS event");
                        countDownLatch.countDown();

                        Settings settings = new Settings(asList(
                                new Setting("key", SettingType.string, "value"),
                                new Setting("key2", SettingType.bool, "false"),
                                new Setting("key3", SettingType.integer, "1")));

                        SdkQueueEvent settingsResponse = SdkQueueEvent.response(new SDKResponse(true, settings), toOtag);
                        SERVICE_QUEUE.put(settingsResponse);
                    }
                } catch (Throwable t) {
                    // log and ignore
                    SdkEventBusLog.error("Ignoring error", t);
                }
            }
        };
    }

}
