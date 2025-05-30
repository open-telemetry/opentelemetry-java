/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import java.util.List;

/**
 * Point data with a {@code double} aggregation value.
 *
 * @since 1.14.0
 */
public interface DoublePointData extends PointData {

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static DoublePointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double value,
      List<DoubleExemplarData> exemplars) {
    return ImmutableDoublePointData.create(
        startEpochNanos, epochNanos, attributes, value, exemplars);
  }

  /** Returns the value of the data point. */
  double getValue();

  /** List of exemplars collected from measurements aggregated into this point. */
  @Override
  List<DoubleExemplarData> getExemplars();
}
