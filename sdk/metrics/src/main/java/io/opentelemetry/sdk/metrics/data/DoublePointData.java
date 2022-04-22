/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;

/** A single data point in a timeseries that describes the time-varying value of a double metric. */
public interface DoublePointData extends PointData {
  /**
   * Returns the value of the data point.
   *
   * @return the value of the data point.
   */
  double getValue();

  /** List of exemplars collected from measurements that were used to form the data point. */
  @Override
  List<DoubleExemplarData> getExemplars();
}
