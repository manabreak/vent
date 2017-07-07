package me.manabreak.vent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventSystemTest {

    private EventSystem q;

    @Mock
    private EventProcessor<TestEvent> eventProcessor;
    @Mock
    private EventProcessor<TestEvent> consumingEventProcessor;

    @Before
    public void setUp() {
        q = new EventSystem();
        when(eventProcessor.onEvent(any())).thenReturn(false);
        when(consumingEventProcessor.onEvent(any())).thenReturn(true);
    }

    @Test
    public void testEventQueue() {
        q.subscribe(TestEvent.class, eventProcessor);

        TestEvent event = new TestEvent();
        event.message = "Hello";

        q.post(event);
        verify(eventProcessor, never()).onEvent(any());

        q.process();
        verify(eventProcessor).onEvent(eq(event));

        q.process();
        verifyNoMoreInteractions(eventProcessor);

        q.post(new TestEventTwo());
        verifyNoMoreInteractions(eventProcessor);

        q.process();
        verifyNoMoreInteractions(eventProcessor);

        q.unsubscribe(eventProcessor);
        q.post(event);
        q.process();
        verifyNoMoreInteractions(eventProcessor);
    }

    @Test
    public void testImmediate() {
        q.subscribe(TestEvent.class, eventProcessor);

        TestEvent event = new TestEvent();

        q.postImmediate(event);
        verify(eventProcessor).onEvent(eq(event));

        q.process();
        verifyNoMoreInteractions(eventProcessor);
    }

    @Test
    public void testConsuming_consumeFirst() {
        q.subscribe(TestEvent.class, consumingEventProcessor);
        q.subscribe(TestEvent.class, eventProcessor);

        TestEvent event = new TestEvent();

        q.post(event);
        q.process();

        verify(consumingEventProcessor).onEvent(eq(event));
        verify(eventProcessor, never()).onEvent(eq(event));
    }

    @Test
    public void testConsuming_consumeLast() {
        q.subscribe(TestEvent.class, eventProcessor);
        q.subscribe(TestEvent.class, consumingEventProcessor);

        TestEvent event = new TestEvent();

        q.post(event);
        q.process();

        verify(eventProcessor).onEvent(eq(event));
        verify(consumingEventProcessor).onEvent(eq(event));
    }

    @Test
    public void testConsumingImmediate_consumeFirst() {
        q.subscribe(TestEvent.class, consumingEventProcessor);
        q.subscribe(TestEvent.class, eventProcessor);

        TestEvent event = new TestEvent();

        q.postImmediate(event);

        verify(consumingEventProcessor).onEvent(eq(event));
        verify(eventProcessor, never()).onEvent(eq(event));
    }

    @Test
    public void testConsumingImmediate_consumeLast() {
        q.subscribe(TestEvent.class, eventProcessor);
        q.subscribe(TestEvent.class, consumingEventProcessor);

        TestEvent event = new TestEvent();

        q.postImmediate(event);

        verify(eventProcessor).onEvent(eq(event));
        verify(consumingEventProcessor).onEvent(eq(event));
    }

    @Test
    public void testBefore() {
        q.subscribe(TestEvent.class, eventProcessor);

        EventProcessor<TestEvent> beforeProcessor = mock(EventProcessor.class);
        q.before(TestEvent.class, beforeProcessor);

        TestEvent event = new TestEvent();

        q.post(event);
        q.process();

        verify(beforeProcessor).onEvent(eq(event));
    }

    @Test
    public void testAfter() {
        q.subscribe(TestEvent.class, eventProcessor);

        EventProcessor<TestEvent> afterProcessor = mock(EventProcessor.class);
        q.after(TestEvent.class, afterProcessor);

        TestEvent event = new TestEvent();

        q.post(event);
        q.process();

        verify(afterProcessor).onEvent(eq(event));
    }

    @Test
    public void testBefore_Immediate() {
        q.subscribe(TestEvent.class, eventProcessor);

        EventProcessor<TestEvent> beforeProcessor = mock(EventProcessor.class);
        q.before(TestEvent.class, beforeProcessor);

        TestEvent event = new TestEvent();

        q.postImmediate(event);


        verify(beforeProcessor).onEvent(eq(event));
    }

    @Test
    public void testAfter_Immediate() {
        q.subscribe(TestEvent.class, eventProcessor);

        EventProcessor<TestEvent> afterProcessor = mock(EventProcessor.class);
        q.after(TestEvent.class, afterProcessor);

        TestEvent event = new TestEvent();

        q.postImmediate(event);

        verify(afterProcessor).onEvent(eq(event));
    }

    public static class TestEvent {
        public String message;
    }

    public static class TestEventTwo {
        public int foo;
    }
}