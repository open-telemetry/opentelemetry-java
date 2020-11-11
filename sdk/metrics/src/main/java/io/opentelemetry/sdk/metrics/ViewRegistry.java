/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration.Temporality;
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

  /**
   * Create a new {@link io.opentelemetry.sdk.metrics.Batcher} for use in metric recording
   * aggregation.
   */
  Batcher createBatcher(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {

    AggregationConfiguration specification = aggregationChooser.chooseAggregation(descriptor);

    Aggregation aggregation = specification.aggregation();

    if (Temporality.CUMULATIVE == specification.temporality()) {
      return Batchers.getCumulativeAllLabels(
          descriptor, meterProviderSharedState, meterSharedState, aggregation);
    } else if (Temporality.DELTA == specification.temporality()) {
      return Batchers.getDeltaAllLabels(
          descriptor, meterProviderSharedState, meterSharedState, aggregation);
    }
    throw new IllegalStateException("unsupported Temporality: " + specification.temporality());
  }

  void registerView(InstrumentSelector selector, AggregationConfiguration specification) {
    aggregationChooser.addView(selector, specification);
  }
}
