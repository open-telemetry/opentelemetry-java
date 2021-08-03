/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;

/** Stores collected {@link MetricData}. */
public interface MetricStorage {
  /**
   * Collects the metrics from this storage and resets for the next collection period.
   *
   * @param startEpochNanos The start timestamp for this SDK.
   * @param epochNanos The timestamp for this collection.
   */
  List<MetricData> collectAndReset(long startEpochNanos, long epochNanos);
}
