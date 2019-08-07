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
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
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

  // The event queue is built on this {@link Disruptor}.
  private final Disruptor<DisruptorEvent> disruptor;
  private final RingBuffer<DisruptorEvent> ringBuffer;
  private final SpanProcessor spanProcessor;
  private final AtomicBoolean loggedShutdownMessage = new AtomicBoolean(false);
  private volatile boolean isShutdown = false;
  private final boolean blocking;

  // Creates a new EventQueue. Private to prevent creation of non-singleton instance.
  private DisruptorEventQueue(
      Disruptor<DisruptorEvent> disruptor, SpanProcessor spanProcessor, boolean blocking) {
    this.disruptor = disruptor;
    this.ringBuffer = disruptor.getRingBuffer();
    this.spanProcessor = spanProcessor;
    this.blocking = blocking;
  }

  // Creates a new EventQueue. Private to prevent creation of non-singleton instance.
  static DisruptorEventQueue create(
      int bufferSize, WaitStrategy waitStrategy, SpanProcessor spanProcessor, boolean blocking) {
    // Create new Disruptor for processing. Note that Disruptor creates a single thread per
    // consumer (see https://github.com/LMAX-Exchange/disruptor/issues/121 for details);
    // this ensures that the event handler can take unsynchronized actions whenever possible.
    Disruptor<DisruptorEvent> disruptor =
        new Disruptor<>(
            DisruptorEventFactory.INSTANCE,
            bufferSize,
            new ThreadFactoryWithName(WORKER_THREAD_NAME),
            ProducerType.MULTI,
            waitStrategy);
    disruptor.handleEventsWith(
        new DisruptorEventHandler[] {new DisruptorEventHandler(spanProcessor)});
    disruptor.start();

    return new DisruptorEventQueue(disruptor, spanProcessor, blocking);
  }

  // Enqueues an event on the {@link DisruptorEventQueue}.
  void enqueue(ReadableSpan readableSpan, boolean isStart) {
    if (isShutdown) {
      if (!loggedShutdownMessage.getAndSet(true)) {
        logger.log(Level.INFO, "Attempted to enqueue entry after Disruptor shutdown.");
      }
    }

    long sequence;

    if (blocking) {
      sequence = ringBuffer.next();
    } else {
      try {
        sequence = ringBuffer.tryNext();
      } catch (InsufficientCapacityException e) {
        // TODO: Add metrics for dropped events.
        return;
      }
    }

    try {
      DisruptorEvent event = ringBuffer.get(sequence);
      event.setEntry(readableSpan, isStart);
    } finally {
      ringBuffer.publish(sequence);
    }
  }

  // Shuts down the underlying disruptor.
  void shutdown() {
    if (isShutdown) {
      return;
    }
    synchronized (this) {
      if (isShutdown) {
        return;
      }
      isShutdown = true;
      disruptor.shutdown();
      spanProcessor.shutdown();
    }
  }

  // An event in the {@link EventQueue}. Just holds a reference to an EventQueue.Entry.
  private static final class DisruptorEvent {
    @Nullable private ReadableSpan readableSpan = null;
    private boolean isStart = false;

    // Sets the EventQueueEntry associated with this DisruptorEvent.
    void setEntry(ReadableSpan readableSpan, boolean isStart) {
      this.readableSpan = readableSpan;
      this.isStart = isStart;
    }

    // Returns the ReadableSpan associated with this DisruptorEvent.
    @Nullable
    ReadableSpan getReadableSpan() {
      return readableSpan;
    }

    boolean getIsStart() {
      return isStart;
    }
  }

  // Factory for DisruptorEvent.
  private enum DisruptorEventFactory implements EventFactory<DisruptorEvent> {
    INSTANCE;

    @Override
    public DisruptorEvent newInstance() {
      return new DisruptorEvent();
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
      try {
        if (event.getIsStart()) {
          spanProcessor.onStart(readableSpan);
        } else {
          spanProcessor.onEnd(readableSpan);
        }
      } finally {
        // Remove the reference to the previous entry to allow the memory to be gc'ed.
        event.setEntry(null, /* isStart= */ false);
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
