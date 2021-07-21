/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * {@code SdkMeterProvider} implementation for {@link MeterProvider}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * OpenTelemetry}.
 *
 * <p>WARNING: A MetricProducer is stateful. It will only return changes since the last time it was
 * accessed. This means that if more than one {@link
 * io.opentelemetry.sdk.metrics.export.MetricExporter} has a handle to this MetricProducer, the two
 * exporters will not receive copies of the same metric data to export.
 */
public final class SdkMeterProvider implements MeterProvider, MetricProducer {

  private static final Logger LOGGER = Logger.getLogger(SdkMeterProvider.class.getName());
  static final String DEFAULT_METER_NAME = "unknown";
  private final ComponentRegistry<SdkMeter> registry;
  private final MeterProviderSharedState sharedState;

  SdkMeterProvider(Clock clock, Resource resource, ViewRegistry viewRegistry) {
    this.sharedState = MeterProviderSharedState.create(clock, resource, viewRegistry);
    this.registry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo -> new SdkMeter(sharedState, instrumentationLibraryInfo));
  }

  @Override
  public Meter get(String instrumentationName) {
    return meterBuilder(instrumentationName).build();
  }

  @Override
  public Meter get(String instrumentationName, String instrumentationVersion) {
    return meterBuilder(instrumentationName)
        .setInstrumentationVersion(instrumentationVersion)
        .build();
  }

  @Override
  public MeterBuilder meterBuilder(@Nullable String instrumentationName) {
    if (instrumentationName == null || instrumentationName.isEmpty()) {
      LOGGER.fine("Meter requested without instrumentation name.");
      instrumentationName = DEFAULT_METER_NAME;
    }
    return new SdkMeterBuilder(registry, instrumentationName);
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

  /**
   * Returns a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}.
   *
   * @return a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}.
   */
  public static SdkMeterProviderBuilder builder() {
    return new SdkMeterProviderBuilder();
  }
}
