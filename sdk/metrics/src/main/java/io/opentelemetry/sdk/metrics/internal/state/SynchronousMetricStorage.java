/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.view.ImmutableView;
import io.opentelemetry.sdk.metrics.view.View;

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
  static <T> SynchronousMetricStorage create(
      View view, InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrumentDescriptor);
    Aggregator<T> aggregator =
        ((AggregatorFactory) view.getAggregation())
            .createAggregator(instrumentDescriptor, exemplarFilter);
    // We won't be storing this metric.
    if (Aggregator.drop() == aggregator) {
      return empty();
    }
    return new DefaultSynchronousMetricStorage<>(
        metricDescriptor, aggregator, ImmutableView.getAttributesProcessor(view));
  }
}
