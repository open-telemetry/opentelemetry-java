/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link DoubleUpDownCounter} bound to a fixed set of {@link Attributes}, obtained via {@link
 * ExtendedDoubleUpDownCounter#bind(Attributes)}.
 *
 * <p>Binding resolves the underlying timeseries once, so subsequent {@link #add(double)} calls
 * record directly without the per-recording attribute processing and map lookup that {@link
 * DoubleUpDownCounter#add(double, Attributes)} performs. This is useful when the set of attribute
 * combinations is known ahead of time and the same series is recorded to repeatedly.
 */
@ThreadSafe
public interface BoundDoubleUpDownCounter {

  /**
   * Records a value against the bound attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. May be positive, negative or zero.
   */
  void add(double value);

  /**
   * Records a value against the bound attributes, with an explicit {@link Context}.
   *
   * @param value The increment amount. May be positive, negative or zero.
   * @param context The explicit context to associate with this measurement.
   */
  void add(double value, Context context);
}
