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
public abstract class DoublePoint implements Point {
  public static DoublePoint create(
      long startEpochNanos, long epochNanos, Labels labels, double value) {
    return new AutoValue_DoublePoint(startEpochNanos, epochNanos, labels, value);
  }

  DoublePoint() {}

  /**
   * Returns the value of the data point.
   *
   * @return the value of the data point.
   */
  public abstract double getValue();
}
