/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

/**
 * Exemplar filters are used to pre-filter measurements before attempting to store them in a
 * reservoir.
 */
public interface ExemplarFilter {
  /** Returns whether or not a resorvoir should attempt to filter a measurement. */
  boolean shouldSampleMeasurement(long value, Attributes attributes, Context context);

  /** Returns whether or not a resorvoir should attempt to filter a measurement. */
  boolean shouldSampleMeasurement(double value, Attributes attributes, Context context);

  /** A filter that only allows exemplars with traces. */
  static ExemplarFilter sampleWithTraces() {
    return WithTraceExemplarFilter.INSTANCE;
  }
  /** A filter that accepts any measurement. */
  static ExemplarFilter alwaysSample() {
    return AlwaysSampleFilter.INSTANCE;
  }
  /** A filter that accepts no measurements. */
  static ExemplarFilter neverSample() {
    return NeverSampleFilter.INSTANCE;
  }
  /** Returns the default exemplar filter, pulling from environment variables. */
  static ExemplarFilter defaultFilter() {
    String env = System.getenv("OTEL_METRICS_EXEMPLAR_FILTER");

    if (env != null) {
      switch (env) {
        case "NONE":
          return neverSample();
        case "ALL":
          return alwaysSample();
        case "WITH_SAMPLED_TRACE":
          return sampleWithTraces();
        default:
          // TODO: some kind of error.
      }
    }
    return sampleWithTraces();
  }
}
