/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of metric {@link PointData}.
 *
 * @since 1.14.0
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
