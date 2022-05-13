/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistryBuilder;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Builder class for the {@link SdkMeterProvider}. */
public final class SdkMeterProviderBuilder {

  /**
   * By default, the exemplar filter is set to sample with traces.
   *
   * @see #setExemplarFilter(ExemplarFilter)
   */
  private static final ExemplarFilter DEFAULT_EXEMPLAR_FILTER = ExemplarFilter.sampleWithTraces();

  private Clock clock = Clock.getDefault();
  private Resource resource = Resource.getDefault();
  private final ViewRegistryBuilder viewRegistryBuilder = ViewRegistry.builder();
  private final List<RegisteredReader> registeredReaders = new ArrayList<>();
  private ExemplarFilter exemplarFilter = DEFAULT_EXEMPLAR_FILTER;

  SdkMeterProviderBuilder() {}

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkMeterProviderBuilder setClock(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Assign a {@link Resource} to be attached to all metrics created by Meters.
   *
   * @param resource A Resource implementation.
   * @return this
   */
  public SdkMeterProviderBuilder setResource(Resource resource) {
    Objects.requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Assign an {@link ExemplarFilter} for all metrics created by Meters.
   *
   * @return this
   */
  SdkMeterProviderBuilder setExemplarFilter(ExemplarFilter filter) {
    this.exemplarFilter = filter;
    return this;
  }

  /**
   * Register a view with the given {@link InstrumentSelector}.
   *
   * <p>Example on how to register a view:
   *
   * <pre>{@code
   * // create a SdkMeterProviderBuilder
   * SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();
   *
   * // create a selector to select which instruments to customize:
   * InstrumentSelector instrumentSelector = InstrumentSelector.builder()
   *   .setType(InstrumentType.COUNTER)
   *   .build();
   *
   * // register the view with the SdkMeterProviderBuilder
   * meterProviderBuilder.registerView(
   *   instrumentSelector,
   *   View.builder()
   *       .setAggregation(
   *           Aggregation.explicitBucketHistogram(Arrays.asList(10d, 20d, 30d, 40d, 50d)))
   *       .setName("my-view-name")
   *       .setDescription("my-view-description")
   *       .build());
   * }</pre>
   *
   * @since 1.1.0
   */
  public SdkMeterProviderBuilder registerView(InstrumentSelector selector, View view) {
    Objects.requireNonNull(selector, "selector");
    Objects.requireNonNull(view, "view");
    viewRegistryBuilder.addView(
        selector, view, view.getAttributesProcessor(), SourceInfo.fromCurrentStack());
    return this;
  }

  /**
   * Registers a {@link MetricReader} for this SDK.
   *
   * <p>Note: custom implementations of {@link MetricReader} are not currently supported.
   *
   * @param reader The reader.
   * @return this
   */
  public SdkMeterProviderBuilder registerMetricReader(MetricReader reader) {
    registeredReaders.add(RegisteredReader.create(reader));
    return this;
  }

  /**
   * Returns a new {@link SdkMeterProvider} built with the configuration of this {@link
   * SdkMeterProviderBuilder}.
   */
  public SdkMeterProvider build() {
    return new SdkMeterProvider(
        registeredReaders, clock, resource, viewRegistryBuilder.build(), exemplarFilter);
  }
}
