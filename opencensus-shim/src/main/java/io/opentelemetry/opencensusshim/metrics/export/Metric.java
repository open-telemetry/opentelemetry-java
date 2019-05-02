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

package io.opentelemetry.opencensusshim.metrics.export;

import com.google.auto.value.AutoValue;
import io.opentelemetry.opencensusshim.common.ExperimentalApi;
import io.opentelemetry.opencensusshim.internal.Utils;
import io.opentelemetry.opencensusshim.metrics.export.Value.ValueDistribution;
import io.opentelemetry.opencensusshim.metrics.export.Value.ValueDouble;
import io.opentelemetry.opencensusshim.metrics.export.Value.ValueLong;
import io.opentelemetry.opencensusshim.metrics.export.Value.ValueSummary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link Metric} with one or more {@link TimeSeries}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
@Immutable
@AutoValue
public abstract class Metric {

  Metric() {}

  /**
   * Creates a {@link Metric}.
   *
   * @param metricDescriptor the {@link MetricDescriptor}.
   * @param timeSeriesList the {@link TimeSeries} list for this metric.
   * @return a {@code Metric}.
   * @since 0.1.0
   */
  public static Metric create(MetricDescriptor metricDescriptor, List<TimeSeries> timeSeriesList) {
    Utils.checkListElementNotNull(
        Utils.checkNotNull(timeSeriesList, "timeSeriesList"), "timeSeries");
    return createInternal(
        metricDescriptor, Collections.unmodifiableList(new ArrayList<TimeSeries>(timeSeriesList)));
  }

  /**
   * Creates a {@link Metric}.
   *
   * @param metricDescriptor the {@link MetricDescriptor}.
   * @param timeSeries the single {@link TimeSeries} for this metric.
   * @return a {@code Metric}.
   * @since 0.1.0
   */
  public static Metric createWithOneTimeSeries(
      MetricDescriptor metricDescriptor, TimeSeries timeSeries) {
    return createInternal(
        metricDescriptor, Collections.singletonList(Utils.checkNotNull(timeSeries, "timeSeries")));
  }

  /**
   * Creates a {@link Metric}.
   *
   * @param metricDescriptor the {@link MetricDescriptor}.
   * @param timeSeriesList the {@link TimeSeries} list for this metric.
   * @return a {@code Metric}.
   * @since 0.1.0
   */
  private static Metric createInternal(
      MetricDescriptor metricDescriptor, List<TimeSeries> timeSeriesList) {
    Utils.checkNotNull(metricDescriptor, "metricDescriptor");
    checkTypeMatch(metricDescriptor.getType(), timeSeriesList);
    return new AutoValue_Metric(metricDescriptor, timeSeriesList);
  }

  /**
   * Returns the {@link MetricDescriptor} of this metric.
   *
   * @return the {@code MetricDescriptor} of this metric.
   * @since 0.1.0
   */
  public abstract MetricDescriptor getMetricDescriptor();

  /**
   * Returns the {@link TimeSeries} list for this metric.
   *
   * <p>The type of the {@link TimeSeries#getPoints()} must match {@link MetricDescriptor.Type}.
   *
   * @return the {@code TimeSeriesList} for this metric.
   * @since 0.1.0
   */
  public abstract List<TimeSeries> getTimeSeriesList();

  private static void checkTypeMatch(MetricDescriptor.Type type, List<TimeSeries> timeSeriesList) {
    for (TimeSeries timeSeries : timeSeriesList) {
      for (Point point : timeSeries.getPoints()) {
        Value value = point.getValue();
        String valueClassName = "";
        if (value.getClass().getSuperclass() != null) { // work around nullness check
          // AutoValue classes should always have a super class.
          valueClassName = value.getClass().getSuperclass().getSimpleName();
        }
        switch (type) {
          case GAUGE_INT64:
          case CUMULATIVE_INT64:
            Utils.checkArgument(
                value instanceof ValueLong, "Type mismatch: %s, %s.", type, valueClassName);
            break;
          case CUMULATIVE_DOUBLE:
          case GAUGE_DOUBLE:
            Utils.checkArgument(
                value instanceof ValueDouble, "Type mismatch: %s, %s.", type, valueClassName);
            break;
          case GAUGE_DISTRIBUTION:
          case CUMULATIVE_DISTRIBUTION:
            Utils.checkArgument(
                value instanceof ValueDistribution, "Type mismatch: %s, %s.", type, valueClassName);
            break;
          case SUMMARY:
            Utils.checkArgument(
                value instanceof ValueSummary, "Type mismatch: %s, %s.", type, valueClassName);
        }
      }
    }
  }
}
