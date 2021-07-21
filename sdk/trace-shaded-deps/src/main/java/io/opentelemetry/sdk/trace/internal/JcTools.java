/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;

/** Internal accessor of JCTools package for fast queues. */
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

  private JcTools() {}
}
