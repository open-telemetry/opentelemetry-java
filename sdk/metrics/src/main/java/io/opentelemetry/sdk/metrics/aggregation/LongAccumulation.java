/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData;
import javax.annotation.concurrent.Immutable;

/** Accumulation that holds a {@code long} value. */
@Immutable
@AutoValue
public abstract class LongAccumulation implements Accumulation {
  /**
   * Creates a new {@link LongAccumulation} with the given {@code value}.
   *
   * @param value the value of this accumulation.
   * @return a new {@link LongAccumulation} with the given {@code value}.
   */
  public static LongAccumulation create(long value) {
    return new AutoValue_LongAccumulation(value);
  }

  LongAccumulation() {}

  abstract long getValue();

  @Override
  public MetricData.Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    return MetricData.LongPoint.create(startEpochNanos, epochNanos, labels, getValue());
  }
}
