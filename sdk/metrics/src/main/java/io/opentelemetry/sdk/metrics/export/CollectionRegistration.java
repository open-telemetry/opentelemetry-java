/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@link CollectionRegistration} is passed to each {@link MetricReader} registered with {@link
 * SdkMeterProvider}, and provides readers the ability to trigger metric collections.
 *
 * @since 1.14.0
 */
public interface CollectionRegistration {

  /**
   * Returns a noop {@link CollectionRegistration}, useful for {@link MetricReader}s to hold before
   * {@link MetricReader#register(CollectionRegistration)} is called.
   *
   * @since 1.31.0
   */
  static CollectionRegistration noop() {
    return new CollectionRegistration() {};
  }

  /**
   * Collect all metrics, including metrics from the SDK and any registered {@link MetricProducer}s.
   *
   * <p>If {@link MetricReader#getMemoryMode()} is configured to {@link MemoryMode#REUSABLE_DATA} do
   * not keep the result or any of its contained objects as they are to be reused to return the
   * result for the next call to this method.
   *
   * @since 1.31.0
   */
  default Collection<MetricData> collectAllMetrics() {
    return Collections.emptyList();
  }
}
