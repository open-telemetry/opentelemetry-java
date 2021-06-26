/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

/**
 * LongPoint is a single data point in a timeseries that describes the time-varying values of a
 * int64 metric.
 *
 * <p>In the proto definition this is called Int64Point.
 */
@Immutable
@AutoValue
public abstract class LongPointData implements PointData {

  LongPointData() {}

  /**
   * Returns the value of the data point.
   *
   * @return the value of the data point.
   */
  public abstract long getValue();

  public static LongPointData create(
      long startEpochNanos, long epochNanos, Attributes attributes, long value) {
    return new AutoValue_LongPointData(startEpochNanos, epochNanos, attributes, value);
  }
}
