/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.ProtoEnumInfo;
import io.opentelemetry.proto.metrics.v1.internal.AggregationTemporality;

final class MetricsMarshalerUtil {

  static ProtoEnumInfo mapToTemporality(
      io.opentelemetry.sdk.metrics.data.AggregationTemporality temporality) {
    switch (temporality) {
      case CUMULATIVE:
        return AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
      case DELTA:
        return AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
    }
    // NB: Should not be possible with aligned versions.
    return AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;
  }

  private MetricsMarshalerUtil() {}
}
