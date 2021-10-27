/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metrics;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.otlp.internal.metrics.MetricsRequestMarshaler;
import io.opentelemetry.exporter.otlp.internal.okhttp.OkHttpExporterBuilder;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Builder utility for {@link OtlpHttpMetricExporter}. */
public final class OtlpHttpMetricExporterBuilder {

  private static final String DEFAULT_ENDPOINT = "http://localhost:4318/v1/metrics";

  private final OkHttpExporterBuilder<MetricsRequestMarshaler> delegate;

  OtlpHttpMetricExporterBuilder() {
    delegate = new OkHttpExporterBuilder<>("metric", DEFAULT_ENDPOINT);
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of metrics. If
   * unset, defaults to {@value OkHttpExporterBuilder#DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpMetricExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    delegate.setTimeout(timeout, unit);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of metrics. If
   * unset, defaults to {@value OkHttpExporterBuilder#DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpMetricExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the OTLP endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT}. The
   * endpoint must start with either http:// or https://, and include the full HTTP path.
   */
  public OtlpHttpMetricExporterBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    delegate.setEndpoint(endpoint);
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Currently
   * supported compression methods include "gzip" and "none".
   */
  public OtlpHttpMetricExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    checkArgument(
        compressionMethod.equals("gzip") || compressionMethod.equals("none"),
        "Unsupported compression method. Supported compression methods include: gzip, none.");
    delegate.setCompression(compressionMethod);
    return this;
  }

  /** Add header to requests. */
  public OtlpHttpMetricExporterBuilder addHeader(String key, String value) {
    delegate.addHeader(key, value);
    return this;
  }

  /**
   * Sets the certificate chain to use for verifying servers when TLS is enabled. The {@code byte[]}
   * should contain an X.509 certificate collection in PEM format. If not set, TLS connections will
   * use the system default trusted certificates.
   */
  public OtlpHttpMetricExporterBuilder setTrustedCertificates(byte[] trustedCertificatesPem) {
    delegate.setTrustedCertificates(trustedCertificatesPem);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpHttpMetricExporter build() {
    return new OtlpHttpMetricExporter(delegate.build());
  }
}
