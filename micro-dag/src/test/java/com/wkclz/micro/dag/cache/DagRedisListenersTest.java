package com.wkclz.micro.dag.cache;

import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.scheduler.DagScheduler;
import com.wkclz.micro.dag.worker.DagWorkerExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DagRedisListenersTest {

    private static final String INSTANCE_CODE = "INST20261114000001";
    private static final String NODE_CODE = "N_pay";

    private DagWorkerExecutor worker;
    private DagScheduler scheduler;
    private RedisMessageListenerContainer container;
    private DagProperties properties;

    @BeforeEach
    void setUp() {
        worker = mock(DagWorkerExecutor.class);
        scheduler = mock(DagScheduler.class);
        container = mock(RedisMessageListenerContainer.class);
        properties = new DagProperties();
    }

    private DagInterruptListener interruptListener() {
        DagInterruptListener listener = new DagInterruptListener();
        ReflectionTestUtils.setField(listener, "worker", worker);
        ReflectionTestUtils.setField(listener, "redisMessageListenerContainer", container);
        ReflectionTestUtils.setField(listener, "properties", properties);
        return listener;
    }

    private DagWakeupListener wakeupListener() {
        DagWakeupListener listener = new DagWakeupListener();
        ReflectionTestUtils.setField(listener, "scheduler", scheduler);
        ReflectionTestUtils.setField(listener, "redisMessageListenerContainer", container);
        ReflectionTestUtils.setField(listener, "properties", properties);
        return listener;
    }

    private Message message(String payload) {
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(payload.getBytes());
        return message;
    }

    @Test
    void interruptInit_dbChannel_notRegistered() {
        properties.setChannel("db");

        interruptListener().init();

        verify(container, never()).addMessageListener(any(MessageListener.class), any(Topic.class));
    }

    @Test
    void interruptInit_autoChannel_registeredOnInterruptTopic() {
        interruptListener().init();

        ArgumentCaptor<ChannelTopic> captor = ArgumentCaptor.forClass(ChannelTopic.class);
        verify(container).addMessageListener(any(), captor.capture());
        assertEquals(DagChannel.INTERRUPT, captor.getValue().getTopic());
    }

    @Test
    void interrupt_onMessage_runningHere_interrupts() {
        DagInterruptListener listener = interruptListener();
        when(worker.isRunningHere(INSTANCE_CODE, NODE_CODE)).thenReturn(true);

        listener.onMessage(message(INSTANCE_CODE + ":" + NODE_CODE), null);

        verify(worker).interrupt(INSTANCE_CODE, NODE_CODE);
    }

    @Test
    void interrupt_onMessage_notRunningHere_skips() {
        DagInterruptListener listener = interruptListener();
        when(worker.isRunningHere(INSTANCE_CODE, NODE_CODE)).thenReturn(false);

        listener.onMessage(message(INSTANCE_CODE + ":" + NODE_CODE), null);

        verify(worker, never()).interrupt(any(), any());
    }

    @Test
    void interrupt_onMessage_nodeCodeContainsColon_stillParsed() {
        DagInterruptListener listener = interruptListener();
        when(worker.isRunningHere(eq(INSTANCE_CODE), eq("N_pay:sub"))).thenReturn(true);

        listener.onMessage(message(INSTANCE_CODE + ":N_pay:sub"), null);

        verify(worker).interrupt(INSTANCE_CODE, "N_pay:sub");
    }

    @Test
    void interrupt_onMessage_invalidPayload_ignored() {
        DagInterruptListener listener = interruptListener();

        assertDoesNotThrow(() -> listener.onMessage(message("no-colon-payload"), null));

        verify(worker, never()).interrupt(any(), any());
    }

    @Test
    void wakeupInit_dbChannel_notRegistered() {
        properties.setChannel("db");

        wakeupListener().init();

        verify(container, never()).addMessageListener(any(MessageListener.class), any(Topic.class));
    }

    @Test
    void wakeupInit_autoChannel_registeredOnWakeupTopic() {
        wakeupListener().init();

        ArgumentCaptor<ChannelTopic> captor = ArgumentCaptor.forClass(ChannelTopic.class);
        verify(container).addMessageListener(any(), captor.capture());
        assertEquals(DagChannel.WAKEUP, captor.getValue().getTopic());
    }

    @Test
    void wakeup_onMessage_triggersScheduler() {
        DagWakeupListener listener = wakeupListener();

        listener.onMessage(message(String.valueOf(System.currentTimeMillis())), null);

        verify(scheduler).wakeUp();
    }
}
