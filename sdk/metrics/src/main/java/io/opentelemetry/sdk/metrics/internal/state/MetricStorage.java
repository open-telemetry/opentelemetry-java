/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.resources.Resource;

/**
 * Stores collected {@link MetricData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface MetricStorage {

  /** Returns a description of the metric produced in this storage. */
  MetricDescriptor getMetricDescriptor();

  /** Returns the registered reader this storage is associated with. */
  RegisteredReader getRegisteredReader();

  /**
   * Collects the metrics from this storage and resets for the next collection period.
   *
   * <p>Note: This is a stateful operation and will reset any interval-related state for the {@code
   * collector}.
   *
   * @param resource The resource associated with the metrics.
   * @param instrumentationScopeInfo The instrumentation scope generating the metrics.
   * @param startEpochNanos The start timestamp for this SDK.
   * @param epochNanos The timestamp for this collection.
   * @return The {@link MetricData} from this collection period.
   */
  MetricData collectAndReset(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos);

  /**
   * Determines whether this storage is an empty metric storage.
   *
   * <p>Uses the reference comparison since {@link EmptyMetricStorage} is singleton.
   *
   * @return true if is empty.
   */
  default boolean isEmpty() {
    return this == EmptyMetricStorage.INSTANCE;
  }
}
