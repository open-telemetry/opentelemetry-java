/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@code SdkMeterProvider} implementation for {@link MeterProvider}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * OpenTelemetry}.
 */
public class SdkMeterProvider implements MeterProvider, MetricProducer {

  private static final Logger LOGGER = Logger.getLogger(SdkMeterProvider.class.getName());
  static final String DEFAULT_METER_NAME = "unknown";
  private final ComponentRegistry<SdkMeter> registry;
  private final MeterProviderSharedState sharedState;

  SdkMeterProvider(Clock clock, Resource resource, MeasurementProcessor processor) {
    this.sharedState = MeterProviderSharedState.create(clock, resource, processor);
    this.registry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo -> new SdkMeter(sharedState, instrumentationLibraryInfo));
  }

  @Override
  public Meter get(String instrumentationName, String instrumentationVersion, String schemaUrl) {
    if (instrumentationName == null || instrumentationName.isEmpty()) {
      LOGGER.fine("Meter requested without instrumentation name.");
      instrumentationName = DEFAULT_METER_NAME;
    }
    return registry.get(instrumentationName, instrumentationVersion, schemaUrl);
  }

  @Override
  public Collection<MetricData> collectAllMetrics() {
    Collection<SdkMeter> meters = registry.getComponents();
    List<MetricData> result = new ArrayList<>(meters.size());
    for (SdkMeter meter : meters) {
      result.addAll(meter.collectAll(sharedState.getClock().now()));
    }
    return Collections.unmodifiableCollection(result);
  }

  public static SdkMeterProviderBuilder builder() {
    return new SdkMeterProviderBuilder();
  }
}
