/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;

// notes:
//  specify by pieces of the descriptor.
//    instrument type √
//    instrument name  (regex) √
//    instrument value type (?)
//    constant labels (?)
//    units (?)

// what you can choose:
//   aggregation √
//   delta vs. cumulative √
//   all labels vs. a list of labels

/**
 * Central location for Views to be registered. Registration of a view should eventually be done via
 * the {@link io.opentelemetry.sdk.metrics.MeterSdkProvider}.
 */
class ViewRegistry {

  private final AggregationChooser aggregationChooser;

  ViewRegistry() {
    this(new AggregationChooser());
  }

  // VisibleForTesting
  ViewRegistry(AggregationChooser aggregationChooser) {
    this.aggregationChooser = aggregationChooser;
  }

  void registerView(InstrumentSelector selector, AggregationConfiguration specification) {
    aggregationChooser.addView(selector, specification);
  }

  /** Create a new {@link InstrumentProcessor} for use in metric recording aggregation. */
  InstrumentProcessor createBatcher(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {

    AggregationConfiguration specification = aggregationChooser.chooseAggregation(descriptor);

    Aggregation aggregation = specification.aggregation();

    if (MetricData.AggregationTemporality.CUMULATIVE == specification.temporality()) {
      return InstrumentProcessor.getCumulativeAllLabels(
          descriptor, meterProviderSharedState, meterSharedState, aggregation);
    } else if (MetricData.AggregationTemporality.DELTA == specification.temporality()) {
      return InstrumentProcessor.getDeltaAllLabels(
          descriptor, meterProviderSharedState, meterSharedState, aggregation);
    }
    throw new IllegalStateException("unsupported Temporality: " + specification.temporality());
  }
}
