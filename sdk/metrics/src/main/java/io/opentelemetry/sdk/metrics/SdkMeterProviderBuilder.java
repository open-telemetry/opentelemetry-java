/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

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
  private static final ExemplarFilterInternal DEFAULT_EXEMPLAR_FILTER =
      ExemplarFilterInternal.asExemplarFilterInternal(ExemplarFilter.traceBased());

  private Clock clock = Clock.getDefault();
  private Resource resource = Resource.getDefault();
  private final IdentityHashMap<MetricReader, CardinalityLimitSelector> metricReaders =
      new IdentityHashMap<>();
  private final List<MetricProducer> metricProducers = new ArrayList<>();
  private final List<RegisteredView> registeredViews = new ArrayList<>();
  private ExemplarFilterInternal exemplarFilter = DEFAULT_EXEMPLAR_FILTER;
  private ScopeConfiguratorBuilder<MeterConfig> meterConfiguratorBuilder =
      MeterConfig.configuratorBuilder();

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

  /** Set the {@link ExemplarFilter} used for all instruments from all meters. */
  public SdkMeterProviderBuilder setExemplarFilter(ExemplarFilter filter) {
    this.exemplarFilter = ExemplarFilterInternal.asExemplarFilterInternal(filter);
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
   * <p>If {@link #registerMetricReader(MetricReader)} is used, the {@link
   * CardinalityLimitSelector#defaultCardinalityLimitSelector()} is used.
   *
   * @since 1.44.0
   */
  public SdkMeterProviderBuilder registerMetricReader(
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

  /**
   * Set the meter configurator, which computes {@link MeterConfig} for each {@link
   * InstrumentationScopeInfo}.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkMeterProviderUtil#setMeterConfigurator(SdkMeterProviderBuilder, ScopeConfigurator)}.
   *
   * <p>Overrides any matchers added via {@link #addMeterConfiguratorCondition(Predicate,
   * MeterConfig)}.
   *
   * @see MeterConfig#configuratorBuilder()
   */
  SdkMeterProviderBuilder setMeterConfigurator(ScopeConfigurator<MeterConfig> meterConfigurator) {
    this.meterConfiguratorBuilder = meterConfigurator.toBuilder();
    return this;
  }

  /**
   * Adds a condition to the meter configurator, which computes {@link MeterConfig} for each {@link
   * InstrumentationScopeInfo}.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkMeterProviderUtil#addMeterConfiguratorCondition(SdkMeterProviderBuilder, Predicate,
   * MeterConfig)}.
   *
   * <p>Applies after any previously added conditions.
   *
   * <p>If {@link #setMeterConfigurator(ScopeConfigurator)} was previously called, this condition
   * will only be applied if the {@link ScopeConfigurator#apply(Object)} returns null for the
   * matched {@link InstrumentationScopeInfo}(s).
   *
   * @see ScopeConfiguratorBuilder#nameEquals(String)
   * @see ScopeConfiguratorBuilder#nameMatchesGlob(String)
   */
  SdkMeterProviderBuilder addMeterConfiguratorCondition(
      Predicate<InstrumentationScopeInfo> scopeMatcher, MeterConfig meterConfig) {
    this.meterConfiguratorBuilder.addCondition(scopeMatcher, meterConfig);
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
        meterConfiguratorBuilder.build());
  }
}
