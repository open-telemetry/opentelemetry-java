/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.contrib.trace.export;

import com.google.common.util.concurrent.MoreExecutors;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
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
          DisruptorEvent, EventType, ReadableSpan, CountDownLatch>
      TRANSLATOR_THREE_ARG =
          new EventTranslatorThreeArg<DisruptorEvent, EventType, ReadableSpan, CountDownLatch>() {
            @Override
            public void translateTo(
                DisruptorEvent event,
                long sequence,
                EventType arg0,
                ReadableSpan arg1,
                CountDownLatch arg2) {
              event.setEntry(arg0, arg1, arg2);
            }
          };
  private static final EventFactory<DisruptorEvent> EVENT_FACTORY =
      new EventFactory<DisruptorEvent>() {
        @Override
        public DisruptorEvent newInstance() {
          return new DisruptorEvent();
        }
      };

  // The event queue is built on this {@link Disruptor}.
  private final Disruptor<DisruptorEvent> disruptor;
  private final RingBuffer<DisruptorEvent> ringBuffer;
  private final AtomicBoolean loggedShutdownMessage = new AtomicBoolean(false);
  private volatile boolean isShutdown = false;
  private final boolean blocking;

  /**
   * Only one consumer for {@link DisruptorEventQueue#forceFlush()} and {@link
   * DisruptorEventQueue#shutdown()} invocation.
   */
  private static final byte NUM_CONSUMERS = 1;

  enum EventType {
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
    this.disruptor =
        new Disruptor<>(
            EVENT_FACTORY,
            bufferSize,
            new ThreadFactoryWithName(WORKER_THREAD_NAME),
            ProducerType.MULTI,
            waitStrategy);
    disruptor.handleEventsWith(new DisruptorEventHandler(spanProcessor));
    this.ringBuffer = disruptor.start();
    this.blocking = blocking;
  }

  void enqueue(ReadableSpan readableSpan, EventType eventType) {
    enqueue(readableSpan, eventType, null);
  }

  // Enqueues an event on the {@link DisruptorEventQueue}.
  void enqueue(ReadableSpan readableSpan, EventType eventType, CountDownLatch flushLatch) {
    if (isShutdown) {
      if (!loggedShutdownMessage.getAndSet(true)) {
        logger.info("Attempted to enqueue entry after Disruptor shutdown.");
      }
      return;
    }

    if (blocking) {
      ringBuffer.publishEvent(TRANSLATOR_THREE_ARG, eventType, readableSpan, flushLatch);
    } else {
      // TODO: Record metrics if element not added.
      ringBuffer.tryPublishEvent(TRANSLATOR_THREE_ARG, eventType, readableSpan, flushLatch);
    }
  }

  // Shuts down the underlying disruptor.
  void shutdown() {
    enqueueAndLock(EventType.ON_SHUTDOWN);
  }

  // Force to publish the ended spans to the SpanProcessor
  void forceFlush() {
    enqueueAndLock(EventType.ON_FORCE_FLUSH);
  }

  void enqueueAndLock(EventType event) {
    if (isShutdown) {
      return;
    }
    synchronized (this) {
      if (isShutdown) {
        return;
      }
      CountDownLatch shutdownCounter = new CountDownLatch(NUM_CONSUMERS); // only one processor.
      enqueue(null, event, shutdownCounter);
      if (event.equals(EventType.ON_SHUTDOWN)) {
        isShutdown = true;
      }
      try {
        shutdownCounter.await();
      } catch (InterruptedException e) {
        // Preserve the interruption.
        Thread.currentThread().interrupt();
        logger.warning("Thread interrupted, shutdown may not finished.");
      }
    }
  }

  // An event in the {@link EventQueue}. Just holds a reference to an EventQueue.Entry.
  private static final class DisruptorEvent {
    @Nullable private ReadableSpan readableSpan = null;
    @Nullable private EventType eventType = null;
    @Nullable private CountDownLatch flushLatch = null;

    void setEntry(
        @Nullable EventType eventType,
        @Nullable ReadableSpan readableSpan,
        @Nullable CountDownLatch flushLatch) {
      this.readableSpan = readableSpan;
      this.eventType = eventType;
      this.flushLatch = flushLatch;
    }

    @Nullable
    ReadableSpan getReadableSpan() {
      return readableSpan;
    }

    @Nullable
    EventType getEventType() {
      return eventType;
    }

    void countDownFlushLatch() {
      if (flushLatch != null) {
        flushLatch.countDown();
      }
    }
  }

  private static final class DisruptorEventHandler implements EventHandler<DisruptorEvent> {
    private final SpanProcessor spanProcessor;

    private DisruptorEventHandler(SpanProcessor spanProcessor) {
      this.spanProcessor = spanProcessor;
    }

    @Override
    public void onEvent(DisruptorEvent event, long sequence, boolean endOfBatch) {
      final ReadableSpan readableSpan = event.getReadableSpan();
      final EventType eventType = event.getEventType();
      if (eventType == null) {
        logger.warning("Disruptor enqueued null element type.");
        return;
      }
      try {
        switch (eventType) {
          case ON_START:
            spanProcessor.onStart(readableSpan);
            break;
          case ON_END:
            spanProcessor.onEnd(readableSpan);
            break;
          case ON_SHUTDOWN:
            spanProcessor.forceFlush();
            spanProcessor.shutdown();
            event.countDownFlushLatch();
            break;
          case ON_FORCE_FLUSH:
            spanProcessor.forceFlush();
            event.countDownFlushLatch();
            break;
        }
      } finally {
        // Remove the reference to the previous entry to allow the memory to be gc'ed.
        event.setEntry(null, null, null);
      }
    }
  }

  private static final class ThreadFactoryWithName implements ThreadFactory {
    private final String threadName;

    private ThreadFactoryWithName(String threadName) {
      this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
      Thread thread = MoreExecutors.platformThreadFactory().newThread(runnable);
      try {
        thread.setName(threadName);
      } catch (SecurityException e) {
        // OK if we can't set the name in this environment.
      }
      return thread;
    }
  }
}
