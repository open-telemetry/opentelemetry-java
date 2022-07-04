/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;

/**
 * Internal accessor of JCTools package for fast queues.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JcTools {

  /**
   * Returns a new {@link Queue} appropriate for use with multiple producers and a single consumer.
   */
  public static <T> Queue<T> newFixedSizeQueue(int capacity) {
    try {
      return new MpscArrayQueue<>(capacity);
    } catch (java.lang.NoClassDefFoundError e) {
      // Happens when modules such as jdk.unsupported are disabled in a custom JRE distribution
      return new ArrayBlockingQueue<>(capacity);
    }
  }

  /**
   * Returns the capacity of the {@link Queue}. We cast to the implementation so callers do not need
   * to use the shaded classes.
   */
  public static long capacity(Queue<?> queue) {
    if (queue instanceof MessagePassingQueue) {
      return ((MessagePassingQueue<?>) queue).capacity();
    } else {
      return (long) ((ArrayBlockingQueue<?>) queue).remainingCapacity() + queue.size();
    }
  }

  /**
   * Remove up to <i>maxExportBatchSize</i> elements from the {@link Queue} and hand to consume.
   *
   * @throws IllegalArgumentException consumer is {@code null}
   * @throws IllegalArgumentException if maxExportBatchSize is negative
   */
  @SuppressWarnings("unchecked")
  public static <T> void drain(Queue<T> queue, int maxExportBatchSize, Consumer<T> consumer) {
    if (queue instanceof MessagePassingQueue) {
      ((MessagePassingQueue<T>) queue).drain(consumer::accept, maxExportBatchSize);
    } else {
      drainNonJcQueue(queue, maxExportBatchSize, consumer);
    }
  }

  private static <T> void drainNonJcQueue(
      Queue<T> queue, int maxExportBatchSize, Consumer<T> consumer) {
    int polledCount = 0;
    T span;
    while (polledCount++ < maxExportBatchSize && (span = queue.poll()) != null) {
      consumer.accept(span);
    }
  }

  private JcTools() {}
}
