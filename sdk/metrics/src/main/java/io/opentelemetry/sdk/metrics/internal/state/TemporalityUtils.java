/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import javax.annotation.Nullable;

final class TemporalityUtils {
  private TemporalityUtils() {}

  /**
   * Resolves which aggregation temporality to use for a given measurement.
   *
   * @param preferred The preferred temporality of the exporter.
   */
  static AggregationTemporality resolveTemporality(@Nullable AggregationTemporality preferred) {
    // If preferred temporality is provided use it, else default to cumulative
    return preferred == null ? AggregationTemporality.CUMULATIVE : preferred;
  }
}
