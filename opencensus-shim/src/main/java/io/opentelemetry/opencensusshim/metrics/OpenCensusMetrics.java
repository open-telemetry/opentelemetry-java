/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import io.opentelemetry.sdk.metrics.export.MetricReader;

/** Convenience methods for adapting OpenCensus metrics into OpenTelemetry. */
public final class OpenCensusMetrics {
  private OpenCensusMetrics() {}

  /**
   * Attaches OpenCensus metrics to metrics read by the given input.
   *
   * @param input A {@link MetricReader} that will receive OpenCensus metrics.
   * @return The adapted MetricReaderFactory.
   */
  public static MetricReader attachTo(MetricReader input) {
    return new OpenCensusAttachingMetricReader(input);
  }
}
