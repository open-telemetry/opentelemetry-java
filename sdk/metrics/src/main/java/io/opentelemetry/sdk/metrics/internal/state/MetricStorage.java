/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;

/**
 * Stores collected {@link MetricData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface MetricStorage {

  /** The default max number of distinct metric points for a particular {@link MetricStorage}. */
  int DEFAULT_MAX_CARDINALITY = 2000;

  /** Attributes capturing overflow measurements recorded when cardinality limit is exceeded. */
  Attributes CARDINALITY_OVERFLOW = Attributes.builder().put("otel.metric.overflow", true).build();

  /** Returns a description of the metric produced in this storage. */
  MetricDescriptor getMetricDescriptor();

  /**
   * Collects the metrics from this storage. If storing {@link AggregationTemporality#DELTA}
   * metrics, reset for the next collection period.
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
  MetricData collect(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos);

  void setEnabled(boolean enabled);
}
