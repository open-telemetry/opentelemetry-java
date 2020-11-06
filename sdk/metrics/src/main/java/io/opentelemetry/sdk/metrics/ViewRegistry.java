/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration.Temporality;
import io.opentelemetry.sdk.metrics.view.Aggregations;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

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

  private static final AggregationConfiguration CUMULATIVE_SUM =
      AggregationConfiguration.create(Aggregations.sum(), Temporality.CUMULATIVE);
  private static final AggregationConfiguration DELTA_SUMMARY =
      AggregationConfiguration.create(Aggregations.minMaxSumCount(), Temporality.DELTA);
  private static final AggregationConfiguration CUMULATIVE_LAST_VALUE =
      AggregationConfiguration.create(Aggregations.lastValue(), Temporality.CUMULATIVE);
  private static final AggregationConfiguration DELTA_LAST_VALUE =
      AggregationConfiguration.create(Aggregations.lastValue(), Temporality.DELTA);

  private final Map<InstrumentSelector, AggregationConfiguration> configuration =
      new ConcurrentHashMap<>();

  /**
   * Create a new {@link io.opentelemetry.sdk.metrics.Batcher} for use in metric recording
   * aggregation.
   */
  Batcher createBatcher(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {

    AggregationConfiguration specification = findBestMatch(descriptor);

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

  // todo: consider moving this method to its own class, for more targeted testing.
  private AggregationConfiguration findBestMatch(InstrumentDescriptor descriptor) {

    for (Map.Entry<InstrumentSelector, AggregationConfiguration> entry : configuration.entrySet()) {
      InstrumentSelector registeredSelector = entry.getKey();

      if (matchesOnType(descriptor, registeredSelector)
          && matchesOnName(descriptor, registeredSelector)) {
        return entry.getValue();
      }
    }

    // If none found, use the defaults:
    return getDefaultSpecification(descriptor);
  }

  private static boolean matchesOnType(
      InstrumentDescriptor descriptor, InstrumentSelector registeredSelector) {
    if (registeredSelector.instrumentType() == null) {
      return true;
    }
    return registeredSelector.instrumentType().equals(descriptor.getType());
  }

  private static boolean matchesOnName(
      InstrumentDescriptor descriptor, InstrumentSelector registeredSelector) {
    Pattern pattern = registeredSelector.instrumentNamePattern();
    if (pattern == null) {
      return true;
    }
    return pattern.matcher(descriptor.getName()).matches();
  }

  private static AggregationConfiguration getDefaultSpecification(InstrumentDescriptor descriptor) {
    switch (descriptor.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
        return CUMULATIVE_SUM;
      case VALUE_RECORDER:
        return DELTA_SUMMARY;
      case VALUE_OBSERVER:
        return DELTA_LAST_VALUE;
      case SUM_OBSERVER:
      case UP_DOWN_SUM_OBSERVER:
        return CUMULATIVE_LAST_VALUE;
    }
    throw new IllegalArgumentException("Unknown descriptor type: " + descriptor.getType());
  }

  void registerView(InstrumentSelector selector, AggregationConfiguration specification) {
    configuration.put(selector, specification);
  }
}
