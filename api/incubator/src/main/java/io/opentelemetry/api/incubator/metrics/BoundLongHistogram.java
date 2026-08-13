/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link LongHistogram} bound to a fixed set of {@link Attributes}, obtained via {@link
 * ExtendedLongHistogram#bind(Attributes)}.
 *
 * <p>Binding resolves the underlying timeseries once, so subsequent {@link #record(long)} calls
 * record directly without the per-recording attribute processing and map lookup that {@link
 * LongHistogram#record(long, Attributes)} performs. This is useful when the set of attribute
 * combinations is known ahead of time and the same series is recorded to repeatedly.
 */
@ThreadSafe
public interface BoundLongHistogram {

  /**
   * Records a value against the bound attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The amount of the measurement. MUST be non-negative.
   */
  void record(long value);

  /**
   * Records a value against the bound attributes, with an explicit {@link Context}.
   *
   * @param value The amount of the measurement. MUST be non-negative.
   * @param context The explicit context to associate with this measurement.
   */
  void record(long value, Context context);
}
