/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.MeterSelector;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;

/**
 * Central location for Views to be registered. Registration of a view is done via the {@link
 * SdkMeterProviderBuilder}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public final class ViewRegistry {
  static final View CUMULATIVE_SUM =
      View.builder()
          .setAggregatorFactory(AggregatorFactory.sum(AggregationTemporality.CUMULATIVE))
          .build();
  static final View DEFAULT_HISTOGRAM =
      View.builder()
          .setAggregatorFactory(
              AggregatorFactory.histogram(
                  Arrays.asList(
                      5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 750d, 1_000d, 2_500d, 5_000d,
                      7_500d, 10_000d),
                  AggregationTemporality.CUMULATIVE))
          .build();
  static final View LAST_VALUE =
      View.builder().setAggregatorFactory(AggregatorFactory.lastValue()).build();

  private final LinkedHashMap<InstrumentSelector, View> configuration;

  ViewRegistry(LinkedHashMap<InstrumentSelector, View> configuration) {
    this.configuration = new LinkedHashMap<>();
    // make a copy for safety
    configuration.forEach((selector, view) -> this.configuration.put(selector, view));
  }

  /** Returns a builder of {@link ViewRegistry}. */
  public static ViewRegistryBuilder builder() {
    return new ViewRegistryBuilder();
  }

  /**
   * Returns the metric {@link View} for a given instrument.
   *
   * @param descriptor description of the instrument.
   * @return The {@link View} for this instrument, or a default aggregation view.
   */
  public View findView(InstrumentDescriptor descriptor, InstrumentationLibraryInfo meter) {
    for (Map.Entry<InstrumentSelector, View> entry : configuration.entrySet()) {
      if (matchesSelector(entry.getKey(), descriptor, meter)) {
        return entry.getValue();
      }
    }

    return getDefaultSpecification(descriptor);
  }

  // Matches an instrument selector against an instrument + meter.
  private static boolean matchesSelector(
      InstrumentSelector selector,
      InstrumentDescriptor descriptor,
      InstrumentationLibraryInfo meter) {
    return (selector.getInstrumentType() == null
            || selector.getInstrumentType() == descriptor.getType())
        && matchesPattern(selector.getInstrumentNamePattern(), descriptor.getName())
        && matchesMeter(selector.getMeterSelector(), meter);
  }

  // Matches a meter selector against a meter.
  private static boolean matchesMeter(MeterSelector selector, InstrumentationLibraryInfo meter) {
    return matchesPattern(selector.getNamePattern(), meter.getName())
        && matchesPattern(selector.getVersionPattern(), meter.getVersion())
        && matchesPattern(selector.getSchemaUrlPattern(), meter.getSchemaUrl());
  }

  // Matches a pattern against  a value.  Null values are treated as empty strings.
  private static boolean matchesPattern(Pattern pattern, String value) {
    if (value == null) {
      return pattern.matcher("").matches();
    }
    return pattern.matcher(value).matches();
  }

  private static View getDefaultSpecification(InstrumentDescriptor descriptor) {
    switch (descriptor.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
      case OBSERVABLE_SUM:
      case OBSERVABLE_UP_DOWN_SUM:
        return CUMULATIVE_SUM;
      case HISTOGRAM:
        return DEFAULT_HISTOGRAM;
      case OBSERVABLE_GAUGE:
        return LAST_VALUE;
    }
    throw new IllegalArgumentException("Unknown descriptor type: " + descriptor.getType());
  }
}
