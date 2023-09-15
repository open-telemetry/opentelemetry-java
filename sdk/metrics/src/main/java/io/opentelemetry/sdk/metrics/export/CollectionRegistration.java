/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;

/**
 * A {@link CollectionRegistration} is passed to each {@link MetricReader} registered with {@link
 * SdkMeterProvider}, and provides readers the ability to trigger metric collections.
 *
 * @since 1.14.0
 */
public interface CollectionRegistration {

  /**
   * Get the {@link MetricProducer}s associated with the {@link SdkMeterProvider}.
   *
   * <p>Note: {@link MetricReader} implementations are expected to call this each time they collect
   * metrics.
   *
   * @since 1.31.0
   */
  default List<MetricProducer> getMetricProducers() {
    return Collections.emptyList();
  }

  /**
   * Returns the resource associated with the {@link SdkMeterProvider}. MUST be used to call {@link
   * MetricProducer#produce(Resource)}.
   *
   * @since 1.31.0
   */
  default Resource getResource() {
    return Resource.getDefault();
  }
}
