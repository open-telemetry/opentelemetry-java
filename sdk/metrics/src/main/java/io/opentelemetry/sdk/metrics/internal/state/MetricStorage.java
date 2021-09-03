/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import java.util.Set;

/**
 * Stores collected {@link MetricData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface MetricStorage {

  /** Returns a description of the metric produced in this storage. */
  MetricDescriptor getMetricDescriptor();
  /**
   * Collects the metrics from this storage and resets for the next collection period.
   *
   * @param startEpochNanos The start timestamp for this SDK.
   * @param epochNanos The timestamp for this collection.
   * @return The {@link MetricData} from this collection period, or {@code null}.
   */
  MetricData collectAndReset(
      CollectionHandle collector,
      Set<CollectionHandle> allCollectors,
      long startEpochNanos,
      long epochNanos);
}
