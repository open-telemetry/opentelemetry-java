/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.otlp.metrics.MetricsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Exports metrics using OTLP via gRPC, using OpenTelemetry's protobuf model.
 *
 * @since 1.14.0
 */
@ThreadSafe
public final class OtlpGrpcMetricExporter implements MetricExporter {

  private final GrpcExporter<MetricsRequestMarshaler> delegate;
  private final AggregationTemporalitySelector aggregationTemporalitySelector;
  private final DefaultAggregationSelector defaultAggregationSelector;

  /**
   * Returns a new {@link OtlpGrpcMetricExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpGrpcMetricExporter} instance.
   */
  public static OtlpGrpcMetricExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcMetricExporterBuilder builder() {
    return new OtlpGrpcMetricExporterBuilder();
  }

  OtlpGrpcMetricExporter(
      GrpcExporter<MetricsRequestMarshaler> delegate,
      AggregationTemporalitySelector aggregationTemporalitySelector,
      DefaultAggregationSelector defaultAggregationSelector) {
    this.delegate = delegate;
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    this.defaultAggregationSelector = defaultAggregationSelector;
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporalitySelector.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return defaultAggregationSelector.getDefaultAggregation(instrumentType);
  }

  /**
   * Submits all the given metrics in a single batch to the OpenTelemetry collector.
   *
   * @param metrics the list of Metrics to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    MetricsRequestMarshaler request = MetricsRequestMarshaler.create(metrics);

    return delegate.export(request, metrics.size());
  }

  /**
   * The OTLP exporter does not batch metrics, so this method will immediately return with success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled. The channel is forcefully closed after a timeout.
   */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }
}
