/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of data points associated to a metric.
 *
 * <p>Loosely equivalent with "Metric" message in OTLP. See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#metric-points
 */
@Immutable
public interface Data<T extends PointData> {
  /**
   * Returns the data {@link PointData}s for this metric.
   *
   * @return the data {@link PointData}s for this metric, or empty {@code Collection} if no points.
   */
  Collection<T> getPoints();
}
