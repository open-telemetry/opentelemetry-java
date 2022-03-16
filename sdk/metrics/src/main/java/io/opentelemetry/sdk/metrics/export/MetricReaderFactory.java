/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;

/**
 * A constructor of {@link MetricReader}s.
 *
 * @deprecated Will be removed without replacement. {@link
 *     SdkMeterProviderBuilder#registerMetricReader(MetricReader)} makes this obsolete.
 */
@Deprecated
public interface MetricReaderFactory {
  /**
   * Construct a new MetricReader.
   *
   * @param producer the mechanism of reading SDK metrics.
   * @return a controller for this metric reader.
   */
  MetricReader apply(MetricProducer producer);
}
