/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;

/**
 * Returns the memory mode selected
 *
 * @since 1.28.0
 */
public interface MemoryModeSelector {
  enum MemoryMode {
    /**
     * Reuses objects to reduce garbage collection
     *
     * In this mode, metric data collected by the {@link MetricReader}, reuses objects
     * to significantly reduce garbage collection, at the expense of
     * disallowing concurrent collection operations.
     * <p>
     * More specifically, the {@link MetricData} objects returned by the registered
     * {@link MetricProducer} are reused across calls to {@link MetricProducer#collectAllMetrics()}
     */
    REUSABLE_DATA,

    /**
     * Uses immutable data structures
     *
     * In this mode, the metric data collected by the reader, is immutable, meant to
     * be used once. This allows running collection operations concurrently, at the
     * expense of increasing garbage collection.
     * <p>
     * More specifically, the {@link MetricData} objects returned by the registered
     * {@link MetricProducer} are immutable.
     */
    IMMUTABLE_DATA
  }

  /**
   * Returns the memory mode used by this instance
   *
   * @return The {@link MemoryMode} used by this instance
   */
  default MemoryMode getMemoryMode() {
    return MemoryMode.IMMUTABLE_DATA;
  }
}
