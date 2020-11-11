/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.Aggregations;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

class AggregationChooser {
  private static final AggregationConfiguration CUMULATIVE_SUM =
      AggregationConfiguration.create(
          Aggregations.sum(), AggregationConfiguration.Temporality.CUMULATIVE);
  private static final AggregationConfiguration DELTA_SUMMARY =
      AggregationConfiguration.create(
          Aggregations.minMaxSumCount(), AggregationConfiguration.Temporality.DELTA);
  private static final AggregationConfiguration CUMULATIVE_LAST_VALUE =
      AggregationConfiguration.create(
          Aggregations.lastValue(), AggregationConfiguration.Temporality.CUMULATIVE);
  private static final AggregationConfiguration DELTA_LAST_VALUE =
      AggregationConfiguration.create(
          Aggregations.lastValue(), AggregationConfiguration.Temporality.DELTA);

  private final Map<InstrumentSelector, AggregationConfiguration> configuration =
      new ConcurrentHashMap<>();

  private static boolean matchesOnType(
      InstrumentDescriptor descriptor, InstrumentSelector registeredSelector) {
    if (registeredSelector.instrumentType() == null) {
      return true;
    }
    return registeredSelector.instrumentType().equals(descriptor.getType());
  }

  AggregationConfiguration chooseAggregation(InstrumentDescriptor descriptor) {

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

  void addView(InstrumentSelector selector, AggregationConfiguration specification) {
    configuration.put(selector, specification);
  }
}
