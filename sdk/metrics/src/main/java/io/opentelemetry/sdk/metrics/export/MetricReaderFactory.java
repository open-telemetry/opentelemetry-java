/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

/** A constructor of {@link MetricReader}s. */
public interface MetricReaderFactory {
  /**
   * Construct a new MetricReader.
   *
   * @param producer the mechanism of reading SDK metrics.
   * @return a controller for this metric reader.
   */
  MetricReader apply(MetricProducer producer);
}
