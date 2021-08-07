/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;

/**
 * Central location for Views to be registered. Registration of a view is done via the {@link
 * SdkMeterProviderBuilder}.
 */
@Immutable
public final class ViewRegistry {
  static final View CUMULATIVE_SUM =
      View.builder()
          .setAggregatorFactory(AggregatorFactory.sum(AggregationTemporality.CUMULATIVE))
          .build();
  static final View SUMMARY =
      View.builder().setAggregatorFactory(AggregatorFactory.minMaxSumCount()).build();
  static final View LAST_VALUE =
      View.builder().setAggregatorFactory(AggregatorFactory.lastValue()).build();

  private final EnumMap<InstrumentType, LinkedHashMap<Pattern, View>> configuration;

  ViewRegistry(EnumMap<InstrumentType, LinkedHashMap<Pattern, View>> configuration) {
    this.configuration = new EnumMap<>(InstrumentType.class);
    // make a copy for safety
    configuration.forEach(
        (instrumentType, patternViewLinkedHashMap) ->
            this.configuration.put(instrumentType, new LinkedHashMap<>(patternViewLinkedHashMap)));
  }

  public static ViewRegistryBuilder builder() {
    return new ViewRegistryBuilder();
  }

  public View findView(InstrumentDescriptor descriptor) {
    LinkedHashMap<Pattern, View> configPerType = configuration.get(descriptor.getType());
    for (Map.Entry<Pattern, View> entry : configPerType.entrySet()) {
      if (entry.getKey().matcher(descriptor.getName()).matches()) {
        return entry.getValue();
      }
    }

    return getDefaultSpecification(descriptor);
  }

  private static View getDefaultSpecification(InstrumentDescriptor descriptor) {
    switch (descriptor.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
      case OBSERVABLE_SUM:
      case OBSERVABLE_UP_DOWN_SUM:
        return CUMULATIVE_SUM;
      case HISTOGRAM:
        return SUMMARY;
      case OBSERVABLE_GAUGE:
        return LAST_VALUE;
    }
    throw new IllegalArgumentException("Unknown descriptor type: " + descriptor.getType());
  }
}
