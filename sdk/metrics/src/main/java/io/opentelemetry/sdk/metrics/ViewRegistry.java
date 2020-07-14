/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.Aggregations;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.ViewSpecification;
import io.opentelemetry.sdk.metrics.view.ViewSpecification.Temporality;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// notes:
//  specify by pieces of the descriptor.
//    instrument type
//    instrument value type
//    instrument name  (wildcards allowed?)
//    constant labels (?)
//    units (?)

// what you can choose:
//   aggregation
//   all labels vs. a list of labels
//   delta vs. cumulative

/**
 * Central location for Views to be registered. Registration of a view should eventually be done via
 * the {@link io.opentelemetry.sdk.metrics.MeterSdkProvider}.
 */
class ViewRegistry {

  private static final ViewSpecification CUMULATIVE_SUM =
      ViewSpecification.create(Aggregations.sum(), Temporality.CUMULATIVE);
  private static final ViewSpecification DELTA_SUMMARY =
      ViewSpecification.create(Aggregations.minMaxSumCount(), Temporality.DELTA);
  private static final ViewSpecification CUMULATIVE_LAST_VALUE =
      ViewSpecification.create(Aggregations.lastValue(), Temporality.CUMULATIVE);

  private final Map<InstrumentSelector, ViewSpecification> configuration =
      new ConcurrentHashMap<>();

  /**
   * Create a new {@link io.opentelemetry.sdk.metrics.Batcher} for use in metric recording
   * aggregation.
   */
  Batcher createBatcher(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {

    ViewSpecification specification = findBestMatch(descriptor);

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

  // todo: consider moving this method to its own class, for more targetted testing.
  private ViewSpecification findBestMatch(InstrumentDescriptor descriptor) {
    // select based on InstrumentType:
    for (Map.Entry<InstrumentSelector, ViewSpecification> entry : configuration.entrySet()) {
      InstrumentSelector registeredSelector = entry.getKey();
      if (registeredSelector.instrumentType().equals(descriptor.getType())) {
        return entry.getValue();
      }
    }
    // If none found, use the defaults:
    return getDefaultSpecification(descriptor);
  }

  private static ViewSpecification getDefaultSpecification(InstrumentDescriptor descriptor) {
    switch (descriptor.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
        return Aggregations.sum();
      case VALUE_RECORDER:
        return Aggregations.minMaxSumCount();
      case VALUE_OBSERVER:
      case SUM_OBSERVER:
      case UP_DOWN_SUM_OBSERVER:
        return CUMULATIVE_LAST_VALUE;
    }
    throw new IllegalArgumentException("Unknown descriptor type: " + descriptor.getType());
  }

  void registerView(InstrumentSelector selector, ViewSpecification specification) {
    configuration.put(selector, specification);
  }
}
