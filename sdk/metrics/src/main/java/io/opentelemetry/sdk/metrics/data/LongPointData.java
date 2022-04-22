/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;

public interface LongPointData extends PointData {
  /**
   * Returns the value of the data point.
   *
   * @return the value of the data point.
   */
  long getValue();

  /** List of exemplars collected from measurements that were used to form the data point. */
  @Override
  List<LongExemplarData> getExemplars();
}
