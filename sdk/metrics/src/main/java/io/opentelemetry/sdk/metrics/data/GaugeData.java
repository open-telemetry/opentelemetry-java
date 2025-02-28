/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * Data for a {@link MetricDataType#LONG_GAUGE} or {@link MetricDataType#DOUBLE_GAUGE} metric.
 *
 * @since 1.14.0
 */
@Immutable
public interface GaugeData<T extends PointData> extends Data<T> {

  /**
   * Creates an instance of {@link ImmutableGaugeData} with the given collection of points.
   *
   * @param <T> the type of the point data
   * @param points the collection of points to be included in the gauge data
   * @return an instance of {@link ImmutableGaugeData} containing the provided points
   */
  static <T extends PointData> ImmutableGaugeData<T> create(Collection<T> points) {
    return ImmutableGaugeData.create(points);
  }
}
