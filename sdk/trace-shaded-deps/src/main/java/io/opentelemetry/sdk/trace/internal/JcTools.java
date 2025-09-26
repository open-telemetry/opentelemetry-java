/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import java.util.Queue;
import java.util.function.Consumer;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;

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
    return new MpscAtomicArrayQueue<>(capacity);
  }

  /**
   * Returns the capacity of the {@link Queue}. We cast to the implementation so callers do not need
   * to use the shaded classes.
   */
  public static long capacity(Queue<?> queue) {
    return ((MessagePassingQueue<?>) queue).capacity();
  }

  /**
   * Remove up to <i>limit</i> elements from the {@link Queue} and hand to consume.
   *
   * @throws IllegalArgumentException consumer is {@code null}
   * @throws IllegalArgumentException if maxExportBatchSize is negative
   */
  @SuppressWarnings("unchecked")
  public static <T> int drain(Queue<T> queue, int limit, Consumer<T> consumer) {
    return ((MessagePassingQueue<T>) queue).drain(consumer::accept, limit);
  }

  private JcTools() {}
}
