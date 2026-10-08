/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link DoubleCounter} bound to a fixed set of {@link Attributes}, obtained via {@link
 * ExtendedDoubleCounter#bind(Attributes)}.
 *
 * <p>Binding resolves the underlying timeseries once, so subsequent {@link #add(double)} calls
 * record directly without the per-recording attribute processing and map lookup that {@link
 * DoubleCounter#add(double, Attributes)} performs. This is useful when the set of attribute
 * combinations is known ahead of time and the same series is recorded to repeatedly.
 */
@ThreadSafe
public interface BoundDoubleCounter {

  /**
   * Records a value against the bound attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. MUST be non-negative.
   */
  void add(double value);

  /**
   * Records a value against the bound attributes, with an explicit {@link Context}.
   *
   * @param value The increment amount. MUST be non-negative.
   * @param context The explicit context to associate with this measurement.
   */
  void add(double value, Context context);
}
