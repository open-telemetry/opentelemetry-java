/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

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
   */
  static CollectionRegistration noop() {
    return new CollectionRegistration() {
      @Override
      public Collection<MetricData> collectAllMetrics() {
        return Collections.emptyList();
      }
    };
  }

  /**
   * Collect all metrics, including metrics from the SDK and any registered {@link MetricProducer}s.
   */
  default Collection<MetricData> collectAllMetrics() {
    return Collections.emptyList();
  }
}
