/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;

abstract class AbstractAccumulator {
  /**
   * Returns the list of metrics collected.
   *
   * @return returns the list of metrics collected.
   */
  abstract List<MetricData> collectAll(long epochNanos);

  static <T> Aggregator<T> getAggregator(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {
    return meterProviderSharedState
        .getViewRegistry()
        .findView(descriptor)
        .getAggregatorFactory()
        .create(
            meterProviderSharedState.getResource(),
            meterSharedState.getInstrumentationLibraryInfo(),
            descriptor);
  }
}
