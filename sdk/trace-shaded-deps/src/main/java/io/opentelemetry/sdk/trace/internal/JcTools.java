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
   * @throws IllegalArgumentException c is {@code null}
   * @throws IllegalArgumentException if limit is negative
   */
  public static void drain(Queue<?> queue, int maxExportBatchSize, Consumer<?> consumer) {
    if (queue instanceof MessagePassingQueue) {
      ((MessagePassingQueue<?>) queue).drain((MessagePassingQueue.Consumer) consumer, maxExportBatchSize);
    } else {
      int polledCount = 0;
      while (polledCount++ < maxExportBatchSize && (queue.peek()) != null) {
        consumer.accept(queue.poll());
      }
    }
  }

  private JcTools() {}
}
