/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.Aggregations;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import java.util.ArrayList;
import java.util.List;
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

  AggregationConfiguration chooseAggregation(InstrumentDescriptor descriptor) {
    List<Map.Entry<InstrumentSelector, AggregationConfiguration>> possibleMatches =
        new ArrayList<>();
    for (Map.Entry<InstrumentSelector, AggregationConfiguration> entry : configuration.entrySet()) {
      InstrumentSelector registeredSelector = entry.getKey();
      // if it matches everything, return it right away...
      if (matchesOnType(descriptor, registeredSelector)
          && matchesOnName(descriptor, registeredSelector)) {
        return entry.getValue();
      }
      // otherwise throw it into a bucket of possible matches if it matches one of the criteria
      if (matchesOne(descriptor, registeredSelector)) {
        possibleMatches.add(entry);
      }
    }

    if (possibleMatches.isEmpty()) {
      return getDefaultSpecification(descriptor);
    }

    // If no exact matches found, pick the first one that matches something:
    return possibleMatches.get(0).getValue();
  }

  private static boolean matchesOne(InstrumentDescriptor descriptor, InstrumentSelector selector) {
    if (selector.hasInstrumentNameRegex() && !matchesOnName(descriptor, selector)) {
      return false;
    }
    if (selector.hasInstrumentType() && !matchesOnType(descriptor, selector)) {
      return false;
    }
    return true;
  }

  private static boolean matchesOnType(
      InstrumentDescriptor descriptor, InstrumentSelector selector) {
    if (selector.instrumentType() == null) {
      return false;
    }
    return selector.instrumentType().equals(descriptor.getType());
  }

  private static boolean matchesOnName(
      InstrumentDescriptor descriptor, InstrumentSelector registeredSelector) {
    Pattern pattern = registeredSelector.instrumentNamePattern();
    if (pattern == null) {
      return false;
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
