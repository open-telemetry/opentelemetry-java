/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

public interface LongPointData extends PointData {
  /**
   * Returns the value of the data point.
   *
   * @return the value of the data point.
   */
  long getValue();
}
