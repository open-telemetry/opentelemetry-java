/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import java.util.EnumSet;
import javax.annotation.Nullable;

final class TemporalityUtils {
  private TemporalityUtils() {}

  /**
   * Resolves which aggregation temporality to use for a given measurement.
   *
   * @param supported All aggregation temporalities supported by the exporter.
   * @param preferred The preferred temporality of the exporter.
   */
  static AggregationTemporality resolveTemporality(
      EnumSet<AggregationTemporality> supported, @Nullable AggregationTemporality preferred) {
    // Next assume preferred should always win.
    if (preferred != null) {
      return preferred;
    }
    // If the exporter doesn't support the configured temporality (or there was none) and doesn't
    // have a preference, use CUMULATIVE if we can, otherwise DELTA.
    if (supported.contains(AggregationTemporality.CUMULATIVE)) {
      return AggregationTemporality.CUMULATIVE;
    }
    if (supported.contains(AggregationTemporality.DELTA)) {
      return AggregationTemporality.DELTA;
    }
    // Default to cumulative if there are no supported temporalities reported.
    // This is likely a bug in the exporter.
    return AggregationTemporality.CUMULATIVE;
  }
}
