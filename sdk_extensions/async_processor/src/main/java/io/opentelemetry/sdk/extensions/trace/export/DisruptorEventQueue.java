/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.export;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.DaemonThreadFactory;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A low-latency event queue for background updating of (possibly contended) objects. This is
 * intended for use by instrumentation methods to ensure that they do not block foreground
 * activities.
 */
@ThreadSafe
final class DisruptorEventQueue {
  private static final Logger logger = Logger.getLogger(DisruptorEventQueue.class.getName());
  private static final String WORKER_THREAD_NAME = "DisruptorEventQueue_WorkerThread";

  private static final EventTranslatorThreeArg<
          DisruptorEvent, EventType, Object, CompletableResultCode>
      TRANSLATOR_THREE_ARG =
          new EventTranslatorThreeArg<DisruptorEvent, EventType, Object, CompletableResultCode>() {
            @Override
            public void translateTo(
                DisruptorEvent event,
                long sequence,
                EventType eventType,
                Object span,
                CompletableResultCode result) {
              event.setEntry(eventType, span, result);
            }
          };
  private static final EventFactory<DisruptorEvent> EVENT_FACTORY =
      new EventFactory<DisruptorEvent>() {
        @Override
        public DisruptorEvent newInstance() {
          return new DisruptorEvent();
        }
      };

  private final RingBuffer<DisruptorEvent> ringBuffer;
  private final AtomicBoolean loggedShutdownMessage = new AtomicBoolean(false);
  private volatile boolean isShutdown = false;
  private final boolean blocking;

  private enum EventType {
    ON_START,
    ON_END,
    ON_SHUTDOWN,
    ON_FORCE_FLUSH
  }

  // Creates a new EventQueue. Private to prevent creation of non-singleton instance.
  DisruptorEventQueue(
      int bufferSize, WaitStrategy waitStrategy, SpanProcessor spanProcessor, boolean blocking) {
    // Create new Disruptor for processing. Note that Disruptor creates a single thread per
    // consumer (see https://github.com/LMAX-Exchange/disruptor/issues/121 for details);
    // this ensures that the event handler can take unsynchronized actions whenever possible.
    Disruptor<DisruptorEvent> disruptor =
        new Disruptor<>(
            EVENT_FACTORY,
            bufferSize,
            new DaemonThreadFactory(WORKER_THREAD_NAME),
            ProducerType.MULTI,
            waitStrategy);
    disruptor.handleEventsWith(new DisruptorEventHandler(spanProcessor));
    this.ringBuffer = disruptor.start();
    this.blocking = blocking;
  }

  void enqueueStartEvent(ReadWriteSpan span, Context parentContext) {
    if (isShutdown) {
      if (!loggedShutdownMessage.getAndSet(true)) {
        logger.info("Attempted to enqueue start event after Disruptor shutdown.");
      }
      return;
    }
    enqueue(EventType.ON_START, new AbstractMap.SimpleImmutableEntry<>(span, parentContext), null);
  }

  void enqueueEndEvent(ReadableSpan span) {
    if (isShutdown) {
      if (!loggedShutdownMessage.getAndSet(true)) {
        logger.info("Attempted to enqueue end event after Disruptor shutdown.");
      }
      return;
    }
    enqueue(EventType.ON_END, span, null);
  }

  // Shuts down the underlying disruptor. Ensures that when this method returns the disruptor is
  // shutdown.
  CompletableResultCode shutdown() {
    synchronized (this) {
      if (isShutdown) {
        // Race condition between two calls to shutdown. The other call already finished.
        return CompletableResultCode.ofSuccess();
      }
      isShutdown = true;
      return enqueueWithResult(EventType.ON_SHUTDOWN);
    }
  }

  // Force to publish the ended spans to the SpanProcessor
  CompletableResultCode forceFlush() {
    if (isShutdown) {
      if (!loggedShutdownMessage.getAndSet(true)) {
        logger.info("Attempted to flush after Disruptor shutdown.");
      }
      return CompletableResultCode.ofFailure();
    }
    return enqueueWithResult(EventType.ON_FORCE_FLUSH);
  }

  private CompletableResultCode enqueueWithResult(EventType event) {
    CompletableResultCode result = new CompletableResultCode();
    enqueue(event, null, result);
    return result;
  }

  // Enqueues an event on the {@link DisruptorEventQueue}.
  private void enqueue(EventType eventType, Object span, CompletableResultCode result) {
    if (blocking) {
      ringBuffer.publishEvent(TRANSLATOR_THREE_ARG, eventType, span, result);
    } else {
      // TODO: Record metrics if element not added.
      ringBuffer.tryPublishEvent(TRANSLATOR_THREE_ARG, eventType, span, result);
    }
  }

  // An event in the {@link EventQueue}. Just holds a reference to an EventQueue.Entry.
  private static final class DisruptorEvent {
    @Nullable private Object eventArgs = null;
    @Nullable private EventType eventType = null;
    @Nullable private CompletableResultCode result = null;

    void setEntry(
        @Nullable EventType eventType,
        @Nullable Object span,
        @Nullable CompletableResultCode result) {
      this.eventArgs = span;
      this.eventType = eventType;
      this.result = result;
    }

    @Nullable
    Object getEventArgs() {
      return eventArgs;
    }

    @Nullable
    EventType getEventType() {
      return eventType;
    }

    void succeed() {
      if (result != null) {
        result.succeed();
      }
    }

    void fail() {
      if (result != null) {
        result.fail();
      }
    }
  }

  private static final class DisruptorEventHandler implements EventHandler<DisruptorEvent> {
    private final SpanProcessor spanProcessor;

    private DisruptorEventHandler(SpanProcessor spanProcessor) {
      this.spanProcessor = spanProcessor;
    }

    @Override
    public void onEvent(final DisruptorEvent event, long sequence, boolean endOfBatch) {
      final Object readableSpan = event.getEventArgs();
      final EventType eventType = event.getEventType();
      if (eventType == null) {
        logger.warning("Disruptor enqueued null element type.");
        return;
      }
      try {
        switch (eventType) {
          case ON_START:
            @SuppressWarnings("unchecked")
            final SimpleImmutableEntry<ReadWriteSpan, Context> eventArgs =
                (SimpleImmutableEntry<ReadWriteSpan, Context>) readableSpan;
            spanProcessor.onStart(eventArgs.getValue(), eventArgs.getKey());
            break;
          case ON_END:
            spanProcessor.onEnd((ReadableSpan) readableSpan);
            break;
          case ON_SHUTDOWN:
            propagateResult(spanProcessor.shutdown(), event);
            break;
          case ON_FORCE_FLUSH:
            propagateResult(spanProcessor.forceFlush(), event);

            break;
        }
      } finally {
        // Remove the reference to the previous entry to allow the memory to be gc'ed.
        event.setEntry(null, null, null);
      }
    }
  }

  private static void propagateResult(
      final CompletableResultCode result, final DisruptorEvent event) {
    result.whenComplete(
        new Runnable() {
          @Override
          public void run() {
            if (result.isSuccess()) {
              event.succeed();
            } else {
              event.fail();
            }
          }
        });
  }
}
