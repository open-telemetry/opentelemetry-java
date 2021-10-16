package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import javax.annotation.Nullable;
import java.util.EnumSet;

final class TemporalityUtils {
  private TemporalityUtils() {}

  /**
   * Resolves which aggregation temporality to use for a given measurement.
   *
   * @param supported All aggregation temporalities supported by the exporter.
   * @param preferred The preferred temporality of the exporter.
   * @param configured The aggregation temporality configured via the View interface.
   */
  static AggregationTemporality resolveTemporality(EnumSet<AggregationTemporality> supported,
      @Nullable AggregationTemporality preferred,
      @Nullable  AggregationTemporality configured) {
    // We assume preferred should always win.
    if (preferred != null) {
      return preferred;
    }
    // Next return the configured temporality, if it exists and is supported.
    if (configured != null && supported.contains(configured)) {
      return configured;
    }
    // If the exporter doesn't support the configured temporality (or there was none),
    // use CUMULATIVE if we can, otherwise DELTA.
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
