package com.opentext.otag.sdk.bus;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class SdkQueueManagerTest {

    private SdkQueueManager instanceMock;
    private String serviceName;
    private String persistenceContext;
    private SdkQueueEvent sdkQueueEventMock;
    private SdkQueueEventId sdkQueueEventId;
    private BlockingQueue queueMock;

    @Before
    public void setUp() throws Exception {
        serviceName = UUID.randomUUID().toString();
        persistenceContext = UUID.randomUUID().toString();
        sdkQueueEventId = new SdkQueueEventId(serviceName, persistenceContext);

        instanceMock = mock(SdkQueueManager.class);
        SdkQueueManager.instance = instanceMock;
        queueMock = mock(BlockingQueue.class);

        instanceMock.SERVICES_QUEUES = mock(Map.class);
        when(instanceMock.SERVICES_QUEUES.get(sdkQueueEventId)).thenReturn(queueMock);

        instanceMock.SERVICES_AGENT_QUEUES = mock(Map.class);
        when(instanceMock.SERVICES_AGENT_QUEUES.get(sdkQueueEventId)).thenReturn(queueMock);

        instanceMock.SERVICE_COMMAND_QUEUES = mock(Map.class);
        when(instanceMock.SERVICE_COMMAND_QUEUES.get(sdkQueueEventId)).thenReturn(queueMock);

        sdkQueueEventMock = mock(SdkQueueEvent.class);
    }

    @Test
    public void registerService() {
        SdkQueueManager.registerService(serviceName, persistenceContext);

        verify(instanceMock).ensureServiceCommandQueue(serviceName, persistenceContext, false);
        verify(instanceMock).ensureServiceQueue(serviceName, persistenceContext, false);

    }

    @Test
    public void retireService() {
        SdkQueueManager.retireService(serviceName, persistenceContext);

        verify(instanceMock.SERVICES_QUEUES).remove(sdkQueueEventId);
        verify(instanceMock.SERVICES_AGENT_QUEUES).remove(sdkQueueEventId);
        verify(instanceMock.SERVICE_COMMAND_QUEUES).remove(sdkQueueEventId);
    }

    @Test
    public void shutdown() {
        assertThat(instanceMock.stop).isFalse();

        SdkQueueManager.shutdown();

        assertThat(instanceMock.stop).isTrue();
    }

    @Test
    public void sendEventToGateway() {
        SdkQueueManager.sendEventToGateway(sdkQueueEventMock);

        verify(instanceMock).put(instanceMock.GATEWAY_QUEUE, "OTAG Q", sdkQueueEventMock);
    }

    @Test
    public void sendCommandToService() {
        SdkQueueManager.sendCommandToService(serviceName, persistenceContext, sdkQueueEventMock);

        verify(instanceMock).ensureServiceCommandQueue(serviceName, persistenceContext, true);
        verify(instanceMock.SERVICE_COMMAND_QUEUES).get(sdkQueueEventId);
        verify(instanceMock).put(queueMock, "COMMAND Q", sdkQueueEventMock);
    }

    @Test
    public void sendEventToService() {
        SdkQueueManager.sendEventToService(serviceName, persistenceContext, sdkQueueEventMock);

        verify(instanceMock).ensureServiceQueue(serviceName, persistenceContext, true);
        verify(instanceMock.SERVICES_QUEUES).get(sdkQueueEventId);
        verify(instanceMock).put(queueMock, "SERVICE RESPONSE Q", sdkQueueEventMock);
    }

    @Test
    public void sendEventToAgent() {
        SdkQueueManager.sendEventToAgent(serviceName, persistenceContext, sdkQueueEventMock);

        verify(instanceMock).ensureServiceAgentQueue(serviceName, persistenceContext, true);
        verify(instanceMock.SERVICES_AGENT_QUEUES).get(sdkQueueEventId);
        verify(instanceMock).put(queueMock, "SERVICE AGENT Q", sdkQueueEventMock);
    }

    @Test
    public void gatewayQueue() {
        BlockingQueue blockingQueueMock = mock(BlockingQueue.class);
        instanceMock.GATEWAY_QUEUE = blockingQueueMock;

        assertThat(SdkQueueManager.gatewayQueue()).isSameAs(blockingQueueMock);
    }

    @Test
    public void getServiceQueue() {
        BlockingQueue blockingQueueMock = mock(BlockingQueue.class);
        when(instanceMock.ensureServiceQueue(serviceName, persistenceContext, true)).thenReturn(blockingQueueMock);

        assertThat(SdkQueueManager.getServiceQueue(serviceName, persistenceContext)).isSameAs(blockingQueueMock);
    }

    @Test
    public void getServiceAgentQueue() {
        BlockingQueue blockingQueueMock = mock(BlockingQueue.class);
        when(instanceMock.ensureServiceAgentQueue(serviceName, persistenceContext, true)).thenReturn(blockingQueueMock);

        assertThat(SdkQueueManager.getServiceAgentQueue(serviceName, persistenceContext)).isSameAs(blockingQueueMock);
    }

    @Test
    public void getServiceCommandQueue() {
        BlockingQueue blockingQueueMock = mock(BlockingQueue.class);
        when(instanceMock.ensureServiceCommandQueue(serviceName, persistenceContext, true)).thenReturn(blockingQueueMock);

        assertThat(SdkQueueManager.getServiceCommandQueue(serviceName, persistenceContext)).isSameAs(blockingQueueMock);
    }

    @Test
    public void putProperlyEnqueuesEvent_happy_path() throws InterruptedException {
        doCallRealMethod().when(instanceMock).put(any(), any(), any());

        instanceMock.put(queueMock, "for logging purposes", sdkQueueEventMock);

        verify(queueMock).put(sdkQueueEventMock);
    }

    @Test
    public void putRetriesTheEnqueueWhenASmallNumberOfInterruptedExceptionsOccur() throws InterruptedException {
        doCallRealMethod().when(instanceMock).put(any(), any(), any());
        doThrow(new InterruptedException()).doThrow(new InterruptedException()).doNothing().when(queueMock).put(any());

        instanceMock.put(queueMock, "for logging purposes", sdkQueueEventMock);

        verify(queueMock, times(3)).put(sdkQueueEventMock);
    }

    @Test
    public void putIsNeverCalledIfServiceShuttingDown() throws InterruptedException {
        doCallRealMethod().when(instanceMock).put(any(), any(), any());

        instanceMock.put(queueMock, "", sdkQueueEventMock);
        instanceMock.put(queueMock, "", sdkQueueEventMock);
        SdkQueueManager.shutdown();
        instanceMock.put(queueMock, "", sdkQueueEventMock);

        verify(queueMock, times(2)).put(any());
    }

    @Test
    public void numberOfRetriesIsASensibleNumber() {
        assertThat(SdkQueueManager.MAX_ENQUEUE_ATTEMPTS).isGreaterThanOrEqualTo(10).isLessThanOrEqualTo(500);
    }

    @Test
    public void putWillRetryAFiniteNumberOfTimesUnderInterruptedExceptions() throws InterruptedException {
        doCallRealMethod().when(instanceMock).put(any(), any(), any());
        doThrow(new InterruptedException()).when(queueMock).put(any());

        instanceMock.put(queueMock, "for logging purposes", sdkQueueEventMock);

        verify(queueMock, times(SdkQueueManager.MAX_ENQUEUE_ATTEMPTS)).put(sdkQueueEventMock);
    }

    @Test
    public void interruptionStatusOfAThreadIsPreserved() throws InterruptedException {
        doCallRealMethod().when(instanceMock).put(any(), any(), any());
        doThrow(new InterruptedException()).doNothing().when(queueMock).put(any());

        instanceMock.put(queueMock, "for logging purposes", sdkQueueEventMock);

        // interrupted() also clears interrupt flag - here this means we don't 'pollute' other tests on this thread.
        assertThat(Thread.interrupted()).isTrue();
    }

    @Test
    public void interruptionStatusOfAThreadIsNotSetWhenNotInterrupted() throws InterruptedException {
        doCallRealMethod().when(instanceMock).put(any(), any(), any());
        doNothing().when(queueMock).put(any());

        instanceMock.put(queueMock, "for logging purposes", sdkQueueEventMock);

        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    @Test
    public void getGatewayQueue() {
        BlockingQueue blockingQueueMock = mock(BlockingQueue.class);
        instanceMock.GATEWAY_QUEUE = blockingQueueMock;

        assertThat(SdkQueueManager.getGatewayQueue()).isSameAs(blockingQueueMock);
    }

    @Test
    public void getServicesQueues() {
        Map mapMock = mock(Map.class);
        instanceMock.SERVICES_QUEUES = mapMock;

        assertThat(SdkQueueManager.getServicesQueues()).isSameAs(mapMock);
    }

    @Test
    public void getServicesAgentQueues() {
        Map mapMock = mock(Map.class);
        instanceMock.SERVICES_AGENT_QUEUES = mapMock;

        assertThat(SdkQueueManager.getServicesAgentQueues()).isSameAs(mapMock);
    }

    @Test
    public void getServiceCommandQueues() {
        Map mapMock = mock(Map.class);
        instanceMock.SERVICE_COMMAND_QUEUES = mapMock;

        assertThat(SdkQueueManager.getServiceCommandQueues()).isSameAs(mapMock);
    }
}
