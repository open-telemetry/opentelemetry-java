/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link DoubleGauge} bound to a fixed set of {@link Attributes}, obtained via {@link
 * ExtendedDoubleGauge#bind(Attributes)}.
 *
 * <p>Binding resolves the underlying timeseries once, so subsequent {@link #set(double)} calls
 * record directly without the per-recording attribute processing and map lookup that {@link
 * DoubleGauge#set(double, Attributes)} performs. This is useful when the set of attribute
 * combinations is known ahead of time and the same series is recorded to repeatedly.
 */
@ThreadSafe
public interface BoundDoubleGauge {

  /**
   * Records the gauge value against the bound attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The current gauge value.
   */
  void set(double value);

  /**
   * Records the gauge value against the bound attributes, with an explicit {@link Context}.
   *
   * @param value The current gauge value.
   * @param context The explicit context to associate with this measurement.
   */
  void set(double value, Context context);
}
