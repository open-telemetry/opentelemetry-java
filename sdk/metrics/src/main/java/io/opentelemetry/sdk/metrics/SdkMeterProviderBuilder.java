/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Builder class for the {@link SdkMeterProvider}.
 *
 * @since 1.14.0
 */
public final class SdkMeterProviderBuilder {

  /**
   * By default, the exemplar filter is set to sample with traces.
   *
   * @see #setExemplarFilter(ExemplarFilter)
   */
  private static final ExemplarFilter DEFAULT_EXEMPLAR_FILTER = ExemplarFilter.traceBased();

  private Clock clock = Clock.getDefault();
  private Resource resource = Resource.getDefault();
  private final IdentityHashMap<MetricReader, CardinalityLimitSelector> metricReaders =
      new IdentityHashMap<>();
  private final List<MetricProducer> metricProducers = new ArrayList<>();
  private final List<RegisteredView> registeredViews = new ArrayList<>();
  private ExemplarFilter exemplarFilter = DEFAULT_EXEMPLAR_FILTER;
  private Function<InstrumentationScopeInfo, MeterConfig> meterConfigProvider =
      unused -> MeterConfig.defaultConfig();

  SdkMeterProviderBuilder() {}

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   */
  public SdkMeterProviderBuilder setClock(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /** Assign a {@link Resource} to be attached to all metrics. */
  public SdkMeterProviderBuilder setResource(Resource resource) {
    Objects.requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Merge a {@link Resource} with the current.
   *
   * @param resource {@link Resource} to merge with current.
   * @since 1.29.0
   */
  public SdkMeterProviderBuilder addResource(Resource resource) {
    Objects.requireNonNull(resource, "resource");
    this.resource = this.resource.merge(resource);
    return this;
  }

  /**
   * Assign an {@link ExemplarFilter} for all metrics created by Meters.
   *
   * <p>Note: not currently stable but available for experimental use via {@link
   * SdkMeterProviderUtil#setExemplarFilter(SdkMeterProviderBuilder, ExemplarFilter)}.
   */
  SdkMeterProviderBuilder setExemplarFilter(ExemplarFilter filter) {
    this.exemplarFilter = filter;
    return this;
  }

  /**
   * Register a {@link View}.
   *
   * <p>The {@code view} influences how instruments which match the {@code selector} are aggregated
   * and exported.
   *
   * <p>For example, the following code registers a view which changes all histogram instruments to
   * aggregate with bucket boundaries different from the default:
   *
   * <pre>{@code
   * // create a SdkMeterProviderBuilder
   * SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();
   *
   * // register the view with the SdkMeterProviderBuilder
   * meterProviderBuilder.registerView(
   *   InstrumentSelector.builder()
   *       .setType(InstrumentType.HISTOGRAM)
   *       .build(),
   *   View.builder()
   *       .setAggregation(
   *           Aggregation.explicitBucketHistogram(Arrays.asList(10d, 20d, 30d, 40d, 50d)))
   *       .build());
   * }</pre>
   */
  public SdkMeterProviderBuilder registerView(InstrumentSelector selector, View view) {
    Objects.requireNonNull(selector, "selector");
    Objects.requireNonNull(view, "view");
    registeredViews.add(
        RegisteredView.create(
            selector,
            view,
            view.getAttributesProcessor(),
            view.getCardinalityLimit(),
            SourceInfo.fromCurrentStack()));
    return this;
  }

  /** Registers a {@link MetricReader}. */
  public SdkMeterProviderBuilder registerMetricReader(MetricReader reader) {
    metricReaders.put(reader, CardinalityLimitSelector.defaultCardinalityLimitSelector());
    return this;
  }

  /**
   * Registers a {@link MetricReader} with a {@link CardinalityLimitSelector}.
   *
   * <p>Note: not currently stable but available for experimental use via {@link
   * SdkMeterProviderUtil#registerMetricReaderWithCardinalitySelector(SdkMeterProviderBuilder,
   * MetricReader, CardinalityLimitSelector)}.
   */
  SdkMeterProviderBuilder registerMetricReader(
      MetricReader reader, CardinalityLimitSelector cardinalityLimitSelector) {
    metricReaders.put(reader, cardinalityLimitSelector);
    return this;
  }

  /**
   * Registers a {@link MetricProducer}.
   *
   * @since 1.31.0
   */
  public SdkMeterProviderBuilder registerMetricProducer(MetricProducer metricProducer) {
    metricProducers.add(metricProducer);
    return this;
  }

  public SdkMeterProviderBuilder setMeterConfigProvider(
      Function<InstrumentationScopeInfo, MeterConfig> meterConfigProvider) {
    this.meterConfigProvider = meterConfigProvider;
    return this;
  }

  /** Returns an {@link SdkMeterProvider} built with the configuration of this builder. */
  public SdkMeterProvider build() {
    return new SdkMeterProvider(
        registeredViews,
        metricReaders,
        metricProducers,
        clock,
        resource,
        exemplarFilter,
        meterConfigProvider);
  }
}
