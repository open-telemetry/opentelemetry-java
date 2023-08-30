/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;

/** The type of memory allocation used during metric collection in {@link MetricReader}. */
public enum MemoryMode {

  /**
   * Reuses objects to reduce garbage collection.
   *
   * <p>In this mode, metric data collection, done by the {@link MetricReader}, reuses objects to
   * significantly reduce garbage collection, at the expense of disallowing concurrent collection
   * operations.
   *
   * <p>More specifically, {@link MetricData} objects returned by the registered {@link
   * MetricProducer} are reused across calls to {@link MetricProducer#collectAllMetrics()}
   */
  REUSABLE_DATA,

  /**
   * Uses immutable data structures.
   *
   * <p>In this mode, the {@link MetricData} collected by the reader, is immutable, meant to be used once.
   * This allows running {@link MetricReader} collection operations concurrently, at the expense of
   * increased garbage collection.
   *
   * <p>More specifically, {@link MetricData} objects returned by the registered {@link
   * MetricProducer} are immutable.
   */
  IMMUTABLE_DATA
}
