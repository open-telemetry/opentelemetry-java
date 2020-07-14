/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * {@code Meter} provider implementation for {@link MeterProvider}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * OpenTelemetry}.
 */
public final class MeterSdkProvider implements MeterProvider {

  private final MeterSdkComponentRegistry registry;
  private final MetricProducer metricProducer;
  private final ViewRegistry viewRegistry = new ViewRegistry();

  private MeterSdkProvider(Clock clock, Resource resource) {
    this.registry =
        new MeterSdkComponentRegistry(
            MeterProviderSharedState.create(clock, resource), viewRegistry);
    this.metricProducer = new MetricProducerSdk(this.registry);
  }

  @Override
  public MeterSdk get(String instrumentationName) {
    return registry.get(instrumentationName);
  }

  @Override
  public MeterSdk get(String instrumentationName, String instrumentationVersion) {
    return registry.get(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns the {@link MetricProducer} that can be used to retrieve metrics from this {@code
   * MeterSdkProvider}.
   *
   * <p>WARNING: A MetricProducer is stateful. It will only return changes since the last time it
   * was accessed. This means that if more than one {@link
   * io.opentelemetry.sdk.metrics.export.MetricExporter} has a handle to this MetricProducer, the
   * two exporters will not receive copies of the same metric data to export.
   *
   * @return the {@link MetricProducer} that can be used to retrieve metrics from this {@code
   *     MeterSdkProvider}.
   */
  public MetricProducer getMetricProducer() {
    return metricProducer;
  }

  /**
   * Returns a new {@link Builder} for {@link MeterSdkProvider}.
   *
   * @return a new {@link Builder} for {@link MeterSdkProvider}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for the {@link MeterSdkProvider}. Has fully functional default implementations of
   * all three required interfaces.
   */
  public static final class Builder {

    private Clock clock = MillisClock.getInstance();
    private Resource resource = Resource.getDefault();

    private Builder() {}

    /**
     * Assign a {@link Clock}.
     *
     * @param clock The clock to use for all temporal needs.
     * @return this
     */
    public Builder setClock(@Nonnull Clock clock) {
      Objects.requireNonNull(clock, "clock");
      this.clock = clock;
      return this;
    }

    /**
     * Assign a {@link Resource} to be attached to all Spans created by Tracers.
     *
     * @param resource A Resource implementation.
     * @return this
     */
    public Builder setResource(@Nonnull Resource resource) {
      Objects.requireNonNull(resource, "resource");
      this.resource = resource;
      return this;
    }

    /**
     * Create a new TracerSdkFactory instance.
     *
     * @return An initialized TracerSdkFactory.
     */
    public MeterSdkProvider build() {
      return new MeterSdkProvider(clock, resource);
    }
  }

  private static final class MeterSdkComponentRegistry extends ComponentRegistry<MeterSdk> {
    private final MeterProviderSharedState meterProviderSharedState;
    private final ViewRegistry viewRegistry;

    private MeterSdkComponentRegistry(
        MeterProviderSharedState meterProviderSharedState, ViewRegistry viewRegistry) {
      this.meterProviderSharedState = meterProviderSharedState;
      this.viewRegistry = viewRegistry;
    }

    @Override
    public MeterSdk newComponent(InstrumentationLibraryInfo instrumentationLibraryInfo) {
      return new MeterSdk(meterProviderSharedState, instrumentationLibraryInfo, viewRegistry);
    }
  }

  /**
   * Register a view with the given {@link InstrumentSelector}.
   *
   * <p>Example on how to register a view:
   *
   * <pre>{@code
   * // get a handle to the MeterSdkProvider
   * MeterSdkProvider meterProvider = OpenTelemetrySdk.getMeterProvider();
   *
   * // create a selector to select which instruments to customize:
   * InstrumentSelector instrumentSelector = InstrumentSelector.create(InstrumentType.COUNTER);
   *
   * // create a specification of how you want the metrics aggregated:
   * ViewSpecification viewSpecification =
   *   ViewSpecification.create(Aggregations.minMaxSumCount(), Temporality.DELTA);
   *
   * //register the view with the MeterSdkProvider
   * meterProvider.registerView(instrumentSelector, viewSpecification);
   * }</pre>
   *
   * @see ViewSpecification
   */
  public void registerView(InstrumentSelector selector, ViewSpecification specification) {
    viewRegistry.registerView(selector, specification);
  }

  private static final class MetricProducerSdk implements MetricProducer {
    private final MeterSdkComponentRegistry registry;

    private MetricProducerSdk(MeterSdkComponentRegistry registry) {
      this.registry = registry;
    }

    @Override
    public Collection<MetricData> collectAllMetrics() {
      Collection<MeterSdk> meters = registry.getComponents();
      List<MetricData> result = new ArrayList<>(meters.size());
      for (MeterSdk meter : meters) {
        result.addAll(meter.collectAll());
      }
      return Collections.unmodifiableCollection(result);
    }
  }
}
