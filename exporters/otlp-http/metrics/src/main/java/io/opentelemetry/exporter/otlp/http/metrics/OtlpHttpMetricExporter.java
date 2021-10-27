/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metrics;

import io.opentelemetry.exporter.otlp.internal.metrics.MetricsRequestMarshaler;
import io.opentelemetry.exporter.otlp.internal.okhttp.OkHttpExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Exports metrics using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpMetricExporter implements MetricExporter {

  private final OkHttpExporter<MetricsRequestMarshaler> delegate;

  OtlpHttpMetricExporter(OkHttpExporter<MetricsRequestMarshaler> delegate) {
    this.delegate = delegate;
  }

  /**
   * Returns a new {@link OtlpHttpMetricExporter} using the default values.
   *
   * @return a new {@link OtlpHttpMetricExporter} instance.
   */
  public static OtlpHttpMetricExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpMetricExporterBuilder builder() {
    return new OtlpHttpMetricExporterBuilder();
  }

  @Nullable
  @Override
  public final AggregationTemporality getPreferredTemporality() {
    // TODO: Lookup based on specification, or constructor
    // https://github.com/open-telemetry/opentelemetry-java/issues/3790
    return null;
  }

  /**
   * Submits all the given metrics in a single batch to the OpenTelemetry collector.
   *
   * @param metrics the list of sampled Metrics to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    MetricsRequestMarshaler exportRequest = MetricsRequestMarshaler.create(metrics);
    return delegate.export(exportRequest, metrics.size());
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

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }
}
