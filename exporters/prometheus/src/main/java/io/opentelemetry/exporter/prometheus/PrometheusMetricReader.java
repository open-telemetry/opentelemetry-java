/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * This is the bridge between Prometheus and OpenTelemetry.
 *
 * <p>The {@link PrometheusMetricReader} is a Prometheus {@link MultiCollector} and can be
 * registered with the {@link io.prometheus.metrics.model.registry.PrometheusRegistry
 * PrometheusRegistry}. It's also an OpenTelemetry {@link MetricReader} and can be registered with a
 * {@link io.opentelemetry.sdk.metrics.SdkMeterProvider SdkMeterProvider}.
 */
public class PrometheusMetricReader implements MetricReader, MultiCollector {

  private volatile CollectionRegistration collectionRegistration = CollectionRegistration.noop();
  private final Otel2PrometheusConverter converter;

  /** Returns a new {@link PrometheusMetricReader} with default configuration. */
  public static PrometheusMetricReader create() {
    return builder().build();
  }

  /** Returns a new {@link PrometheusMetricReaderBuilder}. */
  public static PrometheusMetricReaderBuilder builder() {
    return new PrometheusMetricReaderBuilder();
  }

  /**
   * Deprecated. Use {@link #builder()}.
   *
   * @deprecated use {@link #builder()}.
   */
  @Deprecated
  @SuppressWarnings({"unused", "InconsistentOverloads"})
  public PrometheusMetricReader(
      boolean otelScopeEnabled, @Nullable Predicate<String> allowedResourceAttributesFilter) {
    // otelScopeEnabled parameter was used to control the scope info metric, not scope labels.
    this(
        allowedResourceAttributesFilter,
        /* otelScopeLabelsEnabled= */ true,
        /* otelTargetInfoMetricEnabled= */ true);
  }

  /**
   * Deprecated. Use {@link #builder()}.
   *
   * @deprecated use {@link #builder()}.
   */
  @Deprecated
  public PrometheusMetricReader(@Nullable Predicate<String> allowedResourceAttributesFilter) {
    this(
        allowedResourceAttributesFilter,
        /* otelScopeLabelsEnabled= */ true,
        /* otelTargetInfoMetricEnabled= */ true);
  }

  // Package-private constructor used by builder
  @SuppressWarnings("InconsistentOverloads")
  PrometheusMetricReader(
      @Nullable Predicate<String> allowedResourceAttributesFilter,
      boolean otelScopeLabelsEnabled,
      boolean otelTargetInfoMetricEnabled) {
    this.converter =
        new Otel2PrometheusConverter(
            otelScopeLabelsEnabled, otelTargetInfoMetricEnabled, allowedResourceAttributesFilter);
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return AggregationTemporality.CUMULATIVE;
  }

  @Override
  public void register(CollectionRegistration registration) {
    this.collectionRegistration = registration;
  }

  @Override
  public CompletableResultCode forceFlush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public MetricSnapshots collect() {
    return converter.convert(collectionRegistration.collectAllMetrics());
  }
}
