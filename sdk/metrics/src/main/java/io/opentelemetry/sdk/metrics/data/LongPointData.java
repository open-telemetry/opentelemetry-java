/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import java.util.Collections;
import java.util.List;
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

  /**
   * Creates a {@link LongPointData}.
   *
   * @param startEpochNanos The starting time for the period where this point was sampled. Note:
   *     While start time is optional in OTLP, all SDKs should produce it for all their metrics, so
   *     it is required here.
   * @param epochNanos The ending time for the period when this value was sampled.
   * @param attributes The set of attributes associated with this point.
   * @param value The value that was sampled.
   */
  public static LongPointData create(
      long startEpochNanos, long epochNanos, Attributes attributes, long value) {
    return create(startEpochNanos, epochNanos, attributes, value, Collections.emptyList());
  }

  /**
   * Creates a {@link LongPointData}.
   *
   * @param startEpochNanos The starting time for the period where this point was sampled. Note:
   *     While start time is optional in OTLP, all SDKs should produce it for all their metrics, so
   *     it is required here.
   * @param epochNanos The ending time for the period when this value was sampled.
   * @param attributes The set of attributes associated with this point.
   * @param value The value that was sampled.
   * @param exemplars A collection of interesting sampled values from this time period.
   */
  public static LongPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      long value,
      List<ExemplarData> exemplars) {
    return new AutoValue_LongPointData(startEpochNanos, epochNanos, attributes, exemplars, value);
  }
}
