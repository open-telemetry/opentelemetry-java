/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.trace.Timestamp;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class MetricData {
  MetricData() {}

  /**
   * Returns the {@link MetricDescriptor} of this metric.
   *
   * @return the {@code MetricDescriptor} of this metric.
   * @since 0.1.0
   */
  public abstract MetricDescriptor getMetricDescriptor();

  /**
   * Returns the type of this metric.
   *
   * @return the type of this metric.
   * @since 0.1.0
   */
  public abstract Type getType();

  /**
   * Returns the start {@link Timestamp} of this {@code Metric}, usually the time when the metric
   * was created or an aggregation was enabled.
   *
   * @return the start {@code Timestamp}.
   * @since 0.1.0
   */
  public abstract Timestamp getStartTimestamp();

  /**
   * Returns the {@link Timestamp} when data were collected, usually it represents the moment when
   * {@code Metric.getData()} was called.
   *
   * @return the start {@code Timestamp}.
   * @since 0.1.0
   */
  public abstract Timestamp getTimestamp();

  // TODO: Add TimeSeries/Point

  /**
   * The kind of metric. It describes how the data is reported.
   *
   * @since 0.1.0
   */
  public enum Type {

    /**
     * An instantaneous measurement of an int64 value.
     *
     * @since 0.1.0
     */
    NON_MONOTONIC_INT64,

    /**
     * An instantaneous measurement of a double value.
     *
     * @since 0.1.0
     */
    NON_MONOTONIC_DOUBLE,

    /**
     * An cumulative measurement of an int64 value.
     *
     * @since 0.1.0
     */
    MONOTONIC_INT64,

    /**
     * An cumulative measurement of a double value.
     *
     * @since 0.1.0
     */
    MONOTONIC_DOUBLE,
  }

  static MetricData createInternal(
      MetricDescriptor metricDescriptor, Type type, Timestamp startTimestamp, Timestamp timestamp) {
    return new AutoValue_MetricData(metricDescriptor, type, startTimestamp, timestamp);
  }
}
