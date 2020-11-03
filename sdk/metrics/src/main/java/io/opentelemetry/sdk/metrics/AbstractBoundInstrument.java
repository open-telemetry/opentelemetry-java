/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.SynchronousInstrument.BoundInstrument;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract class that implements the basic the functionality of the BoundInstrument.
 *
 * <p>It atomically counts the number of references (usages) while also keeping a state of
 * mapped/unmapped into an external map. It uses an atomic value where the least significant bit is
 * used to keep the state of mapping ('1' is used for unmapped and '0' is for mapped) and the rest
 * of the bits are used for reference (usage) counting.
 */
abstract class AbstractBoundInstrument implements BoundInstrument {
  // Atomically counts the number of references (usages) while also keeping a state of
  // mapped/unmapped into a registry map.
  private final AtomicLong refCountMapped;
  private final Aggregator aggregator;

  AbstractBoundInstrument(Aggregator aggregator) {
    // Start with this binding already bound.
    this.refCountMapped = new AtomicLong(2);
    this.aggregator = aggregator;
  }

  /**
   * Returns {@code true} if the entry is still mapped and increases the reference usages, if
   * unmapped returns {@code false}.
   *
   * @return {@code true} if successful.
   */
  final boolean bind() {
    // Every reference adds/removes 2 instead of 1 to avoid changing the mapping bit.
    return (refCountMapped.addAndGet(2L) & 1L) == 0;
  }

  @Override
  public final void unbind() {
    // Every reference adds/removes 2 instead of 1 to avoid changing the mapping bit.
    refCountMapped.getAndAdd(-2L);
  }

  /**
   * Flips the mapped bit to "unmapped" state and returns true if both of the following conditions
   * are true upon entry to this function: 1) There are no active references; 2) The mapped bit is
   * in "mapped" state; otherwise no changes are done to mapped bit and false is returned.
   *
   * @return {@code true} if successful.
   */
  final boolean tryUnmap() {
    if (refCountMapped.get() != 0) {
      // Still references (usages) to this bound or already unmapped.
      return false;
    }
    return refCountMapped.compareAndSet(0L, 1L);
  }

  final void recordLong(long value) {
    aggregator.recordLong(value);
  }

  final void recordDouble(double value) {
    aggregator.recordDouble(value);
  }

  final Aggregator getAggregator() {
    return aggregator;
  }
}
