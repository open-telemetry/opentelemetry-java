/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import javax.annotation.concurrent.Immutable;

/**
 * DoublePoint is a single data point in a timeseries that describes the time-varying value of a
 * double metric.
 */
@Immutable
@AutoValue
public abstract class DoublePointData implements PointData {
  public static DoublePointData create(
      long startEpochNanos, long epochNanos, Labels labels, double value) {
    return new AutoValue_DoublePointData(startEpochNanos, epochNanos, labels, value);
  }

  DoublePointData() {}

  /**
   * Returns the value of the data point.
   *
   * @return the value of the data point.
   */
  public abstract double getValue();
}
