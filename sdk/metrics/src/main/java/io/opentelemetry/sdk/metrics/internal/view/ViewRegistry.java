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
import java.util.Collections;
import java.util.List;
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
  static final List<Double> DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES =
      Collections.unmodifiableList(
          Arrays.asList(
              5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 750d, 1_000d, 2_500d, 5_000d, 7_500d,
              10_000d));
  static final View DEFAULT_HISTOGRAM =
      View.builder()
          .setAggregatorFactory(
              AggregatorFactory.histogram(
                  DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES, AggregationTemporality.CUMULATIVE))
          .build();
  static final View LAST_VALUE =
      View.builder().setAggregatorFactory(AggregatorFactory.lastValue()).build();

  private final List<RegisteredView> reverseRegistration;

  ViewRegistry(List<RegisteredView> reverseRegistration) {
    this.reverseRegistration = reverseRegistration;
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
    for (RegisteredView entry : reverseRegistration) {
      if (matchesSelector(entry.getInstrumentSelector(), descriptor, meter)) {
        return entry.getView();
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
        && selector.getInstrumentNameFilter().test(descriptor.getName())
        && matchesMeter(selector.getMeterSelector(), meter);
  }

  // Matches a meter selector against a meter.
  private static boolean matchesMeter(MeterSelector selector, InstrumentationLibraryInfo meter) {
    return selector.getNameFilter().test(meter.getName())
        && selector.getVersionFilter().test(meter.getVersion())
        && selector.getSchemaUrlFilter().test(meter.getSchemaUrl());
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
