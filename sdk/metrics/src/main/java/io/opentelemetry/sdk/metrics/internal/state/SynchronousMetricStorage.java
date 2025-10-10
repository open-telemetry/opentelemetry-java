/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;

/**
 * Stores aggregated {@link MetricData} for synchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface SynchronousMetricStorage extends MetricStorage, WriteableMetricStorage {

  /** Returns metric storage which doesn't store or generate any metrics. */
  static SynchronousMetricStorage empty() {
    return EmptyMetricStorage.INSTANCE;
  }

  /**
   * Constructs metric storage for a given synchronous instrument and view.
   *
   * @return The storage, or {@link EmptyMetricStorage#empty()} if the instrument should not be
   *     recorded.
   */
  static <T extends PointData> SynchronousMetricStorage create(
      RegisteredReader registeredReader,
      RegisteredView registeredView,
      InstrumentDescriptor instrumentDescriptor,
      ExemplarFilter exemplarFilter,
      boolean enabled) {
    View view = registeredView.getView();
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(view, registeredView.getViewSourceInfo(), instrumentDescriptor);
    Aggregator<T> aggregator =
        ((AggregatorFactory) view.getAggregation())
            .createAggregator(
                instrumentDescriptor, exemplarFilter, registeredReader.getReader().getMemoryMode());
    // We won't be storing this metric.
    if (Aggregator.drop() == aggregator) {
      return empty();
    }
    return new DefaultSynchronousMetricStorage<>(
        registeredReader,
        metricDescriptor,
        aggregator,
        registeredView.getViewAttributesProcessor(),
        registeredView.getCardinalityLimit(),
        enabled);
  }
}
