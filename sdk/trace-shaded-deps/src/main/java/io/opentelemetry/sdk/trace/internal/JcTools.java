/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import java.util.Queue;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;

/** Internal accessor of JCTools package for fast queues. */
public final class JcTools {

  /**
   * Returns a new {@link Queue} appropriate for use with multiple producers and a single consumer.
   */
  public static <T> Queue<T> newFixedSizeQueue(int capacity) {
    return new MpscArrayQueue<>(capacity);
  }

  /**
   * Returns the capacity of the {@link Queue}, which must be a JcTools queue. We cast to the
   * implementation so callers do not need to use the shaded classes.
   */
  public static long capacity(Queue<?> queue) {
    return ((MessagePassingQueue<?>) queue).capacity();
  }

  private JcTools() {}
}
