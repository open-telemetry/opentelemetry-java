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
   * Creates a {@link LongPointData}.
   *
   * @param startEpochNanos The starting time for the period where this point was sampled. Note:
   *     While start time is optional in OTLP, all SDKs should produce it for all their metrics, so
   *     it is required here.
   * @param epochNanos The ending time for the period when this value was sampled.
   * @param attributes The set of attributes associated with this point.
   * @param value The value that was sampled.
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
