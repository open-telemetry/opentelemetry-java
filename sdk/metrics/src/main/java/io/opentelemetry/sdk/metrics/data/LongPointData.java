/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import java.util.Collections;
import java.util.List;

/**
 * A point data with a {@code double} aggregation value.
 *
 * @since 1.14.0
 */
public interface LongPointData extends PointData {

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static LongPointData create(
      long startEpochNanos, long epochNanos, Attributes attributes, long value) {
    return ImmutableLongPointData.create(
        startEpochNanos, epochNanos, attributes, value, Collections.emptyList());
  }

  /** Returns the value of the data point. */
  long getValue();

  /** List of exemplars collected from measurements aggregated into this point. */
  @Override
  List<LongExemplarData> getExemplars();
}
