/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.accumulation;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData;
import javax.annotation.concurrent.Immutable;

/** Accumulation that holds a {@code double} value. */
@Immutable
@AutoValue
public abstract class DoubleAccumulation implements Accumulation {
  /**
   * Creates a new {@link DoubleAccumulation} with the given {@code value}.
   *
   * @param value the value of this accumulation.
   * @return a new {@link DoubleAccumulation} with the given {@code value}.
   */
  public static DoubleAccumulation create(double value) {
    return new AutoValue_DoubleAccumulation(value);
  }

  DoubleAccumulation() {}

  /**
   * Returns the {@code double} value stored by this {@link Accumulation}.
   *
   * @return the {@code double} value stored by this {@link Accumulation}.
   */
  public abstract double getValue();

  @Override
  public MetricData.DoublePoint toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    return MetricData.DoublePoint.create(startEpochNanos, epochNanos, labels, getValue());
  }
}
