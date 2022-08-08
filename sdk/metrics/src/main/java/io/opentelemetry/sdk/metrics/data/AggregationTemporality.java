/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

/**
 * Describes the time period over which measurements are aggregated.
 *
 * @since 1.14.0
 */
public enum AggregationTemporality {
  /** Measurements are aggregated since the previous collection. */
  DELTA,
  /** Measurements are aggregated over the lifetime of the instrument. */
  CUMULATIVE
}
