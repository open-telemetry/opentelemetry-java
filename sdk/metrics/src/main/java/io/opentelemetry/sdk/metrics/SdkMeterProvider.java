/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * SDK implementation for {@link MeterProvider}.
 *
 * @since 1.14.0
 */
public final class SdkMeterProvider implements MeterProvider, Closeable {

  private static final Logger LOGGER = Logger.getLogger(SdkMeterProvider.class.getName());
  static final String DEFAULT_METER_NAME = "unknown";

  private final List<RegisteredView> registeredViews;
  private final List<RegisteredReader> registeredReaders;
  private final List<MetricProducer> metricProducers;
  private final MeterProviderSharedState sharedState;
  private final ComponentRegistry<SdkMeter> registry;
  private final AtomicBoolean isClosed = new AtomicBoolean(false);

  private ScopeConfigurator<MeterConfig> meterConfigurator;

  /** Returns a new {@link SdkMeterProviderBuilder} for {@link SdkMeterProvider}. */
  public static SdkMeterProviderBuilder builder() {
    return new SdkMeterProviderBuilder();
  }

  SdkMeterProvider(
      List<RegisteredView> registeredViews,
      IdentityHashMap<MetricReader, CardinalityLimitSelector> metricReaders,
      List<MetricProducer> metricProducers,
      Clock clock,
      Resource resource,
      ExemplarFilterInternal exemplarFilter,
      ScopeConfigurator<MeterConfig> meterConfigurator) {
    long startEpochNanos = clock.now();
    this.registeredViews = registeredViews;
    this.registeredReaders =
        metricReaders.entrySet().stream()
            .map(
                entry ->
                    RegisteredReader.create(
                        entry.getKey(),
                        ViewRegistry.create(entry.getKey(), entry.getValue(), registeredViews)))
            .collect(toList());
    this.metricProducers = metricProducers;
    this.sharedState =
        MeterProviderSharedState.create(clock, resource, exemplarFilter, startEpochNanos);
    this.registry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo ->
                new SdkMeter(
                    sharedState,
                    instrumentationLibraryInfo,
                    registeredReaders,
                    getMeterConfig(instrumentationLibraryInfo)));
    this.meterConfigurator = meterConfigurator;
    for (RegisteredReader registeredReader : registeredReaders) {
      List<MetricProducer> readerMetricProducers = new ArrayList<>(metricProducers);
      readerMetricProducers.add(new LeasedMetricProducer(registry, sharedState, registeredReader));
      registeredReader
          .getReader()
          .register(new SdkCollectionRegistration(readerMetricProducers, sharedState));
      registeredReader.setLastCollectEpochNanos(startEpochNanos);
    }
  }

  private MeterConfig getMeterConfig(InstrumentationScopeInfo instrumentationScopeInfo) {
    MeterConfig meterConfig = meterConfigurator.apply(instrumentationScopeInfo);
    return meterConfig == null ? MeterConfig.defaultConfig() : meterConfig;
  }

  void setMeterConfigurator(ScopeConfigurator<MeterConfig> meterConfigurator) {
    this.meterConfigurator = meterConfigurator;
    this.registry
        .getComponents()
        .forEach(
            sdkMeter ->
                sdkMeter.updateMeterConfig(getMeterConfig(sdkMeter.getInstrumentationScopeInfo())));
  }

  @Override
  public MeterBuilder meterBuilder(String instrumentationScopeName) {
    if (registeredReaders.isEmpty()) {
      return MeterProvider.noop().meterBuilder(instrumentationScopeName);
    }
    if (instrumentationScopeName == null || instrumentationScopeName.isEmpty()) {
      LOGGER.fine("Meter requested without instrumentation scope name.");
      instrumentationScopeName = DEFAULT_METER_NAME;
    }
    return new SdkMeterBuilder(registry, instrumentationScopeName);
  }

  /**
   * Reset the provider, clearing all registered instruments.
   *
   * <p>Note: not currently stable but available for experimental use via {@link
   * SdkMeterProviderUtil#resetForTest(SdkMeterProvider)}.
   */
  void resetForTest() {
    registry.getComponents().forEach(SdkMeter::resetForTest);
  }

  /**
   * Call {@link MetricReader#forceFlush()} on all metric readers associated with this provider. The
   * resulting {@link CompletableResultCode} completes when all complete.
   */
  public CompletableResultCode forceFlush() {
    if (registeredReaders.isEmpty()) {
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>();
    for (RegisteredReader registeredReader : registeredReaders) {
      results.add(registeredReader.getReader().forceFlush());
    }
    return CompletableResultCode.ofAll(results);
  }

  /**
   * Shutdown the provider. Calls {@link MetricReader#shutdown()} on all metric readers associated
   * with this provider. The resulting {@link CompletableResultCode} completes when all complete.
   */
  public CompletableResultCode shutdown() {
    if (!isClosed.compareAndSet(false, true)) {
      LOGGER.info("Multiple close calls");
      return CompletableResultCode.ofSuccess();
    }
    if (registeredReaders.isEmpty()) {
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>();
    for (RegisteredReader info : registeredReaders) {
      results.add(info.getReader().shutdown());
    }
    return CompletableResultCode.ofAll(results);
  }

  /** Close the meter provider. Calls {@link #shutdown()} and blocks waiting for it to complete. */
  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }

  @Override
  public String toString() {
    return "SdkMeterProvider{"
        + "clock="
        + sharedState.getClock()
        + ", resource="
        + sharedState.getResource()
        + ", metricReaders="
        + registeredReaders.stream().map(RegisteredReader::getReader).collect(toList())
        + ", metricProducers="
        + metricProducers
        + ", views="
        + registeredViews
        + ", meterConfigurator="
        + meterConfigurator
        + "}";
  }

  /** Helper class to expose registered metric exports. */
  private static class LeasedMetricProducer implements MetricProducer {

    private final ComponentRegistry<SdkMeter> registry;
    private final MeterProviderSharedState sharedState;
    private final RegisteredReader registeredReader;

    LeasedMetricProducer(
        ComponentRegistry<SdkMeter> registry,
        MeterProviderSharedState sharedState,
        RegisteredReader registeredReader) {
      this.registry = registry;
      this.sharedState = sharedState;
      this.registeredReader = registeredReader;
    }

    @Override
    public Collection<MetricData> produce(Resource unused) {
      Collection<SdkMeter> meters = registry.getComponents();
      List<MetricData> result = new ArrayList<>();
      long collectTime = sharedState.getClock().now();
      for (SdkMeter meter : meters) {
        result.addAll(meter.collectAll(registeredReader, collectTime));
      }
      registeredReader.setLastCollectEpochNanos(collectTime);
      return Collections.unmodifiableCollection(result);
    }
  }

  private static class SdkCollectionRegistration implements CollectionRegistration {
    private final List<MetricProducer> metricProducers;
    private final MeterProviderSharedState sharedState;

    private SdkCollectionRegistration(
        List<MetricProducer> metricProducers, MeterProviderSharedState sharedState) {
      this.metricProducers = metricProducers;
      this.sharedState = sharedState;
    }

    @Override
    public Collection<MetricData> collectAllMetrics() {
      if (metricProducers.isEmpty()) {
        return Collections.emptyList();
      }
      Resource resource = sharedState.getResource();
      if (metricProducers.size() == 1) {
        return metricProducers.get(0).produce(resource);
      }
      List<MetricData> metricData = new ArrayList<>();
      for (MetricProducer metricProducer : metricProducers) {
        metricData.addAll(metricProducer.produce(resource));
      }
      return Collections.unmodifiableList(metricData);
    }
  }
}
