/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.proto.metrics.v1.internal.AggregationTemporality;

final class MetricsMarshalerUtil {

  static int mapToTemporality(
      io.opentelemetry.sdk.metrics.data.AggregationTemporality temporality) {
    switch (temporality) {
      case CUMULATIVE:
        return AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE_VALUE;
      case DELTA:
        return AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA_VALUE;
    }
    return AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED_VALUE;
  }

  private MetricsMarshalerUtil() {}
}
