/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Builder for {@link OtlpGrpcLogExporter}. */
public final class OtlpGrpcLogExporterBuilder {

  private static final String GRPC_SERVICE_NAME =
      "opentelemetry.proto.collector.logs.v1.LogsService";
  // Visible for testing
  static final String GRPC_ENDPOINT_PATH = "/" + GRPC_SERVICE_NAME + "/Export";

  private static final String DEFAULT_ENDPOINT_URL = "http://localhost:4317";
  private static final URI DEFAULT_ENDPOINT = URI.create(DEFAULT_ENDPOINT_URL);
  private static final long DEFAULT_TIMEOUT_SECS = 10;

  // Visible for testing
  final GrpcExporterBuilder<LogsRequestMarshaler> delegate;

  OtlpGrpcLogExporterBuilder() {
    delegate =
        GrpcExporter.builder(
            "log",
            DEFAULT_TIMEOUT_SECS,
            DEFAULT_ENDPOINT,
            () -> MarshalerLogsServiceGrpc::newFutureStub,
            GRPC_SERVICE_NAME,
            GRPC_ENDPOINT_PATH);
  }

  /**
   * Sets the managed chanel to use when communicating with the backend. Takes precedence over
   * {@link #setEndpoint(String)} if both are called.
   *
   * @param channel the channel to use
   * @return this builder's instance
   * @deprecated Use {@link #setEndpoint(String)}. If you have a use case not satisfied by the
   *     methods on this builder, please file an issue to let us know what it is.
   */
  @Deprecated
  public OtlpGrpcLogExporterBuilder setChannel(ManagedChannel channel) {
    delegate.setChannel(channel);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of logs. If unset,
   * defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpGrpcLogExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    delegate.setTimeout(timeout, unit);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of logs. If unset,
   * defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpGrpcLogExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    delegate.setTimeout(timeout);
    return this;
  }

  /**
   * Sets the OTLP endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT_URL}. The
   * endpoint must start with either http:// or https://.
   */
  public OtlpGrpcLogExporterBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    delegate.setEndpoint(endpoint);
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Currently
   * supported compression methods include "gzip" and "none".
   */
  public OtlpGrpcLogExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    checkArgument(
        compressionMethod.equals("gzip") || compressionMethod.equals("none"),
        "Unsupported compression method. Supported compression methods include: gzip, none.");
    delegate.setCompression(compressionMethod);
    return this;
  }

  /**
   * Sets the certificate chain to use for verifying servers when TLS is enabled. The {@code byte[]}
   * should contain an X.509 certificate collection in PEM format. If not set, TLS connections will
   * use the system default trusted certificates.
   */
  public OtlpGrpcLogExporterBuilder setTrustedCertificates(byte[] trustedCertificatesPem) {
    delegate.setTrustedCertificates(trustedCertificatesPem);
    return this;
  }

  /**
   * Sets ths client key and the certificate chain to use for verifying client when TLS is enabled.
   */
  public OtlpGrpcLogExporterBuilder setClientTls(byte[] privateKeyPem, byte[] certificatePem) {
    delegate.setClientTls(privateKeyPem, certificatePem);
    return this;
  }

  /**
   * Add header to request. Optional. Applicable only if {@link
   * OtlpGrpcLogExporterBuilder#setChannel(ManagedChannel)} is not used to set channel.
   *
   * @param key header key
   * @param value header value
   * @return this builder's instance
   */
  public OtlpGrpcLogExporterBuilder addHeader(String key, String value) {
    delegate.addHeader(key, value);
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to collect metrics related to export. If not set, metrics
   * will not be collected.
   */
  public OtlpGrpcLogExporterBuilder setMeterProvider(MeterProvider meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    delegate.setMeterProvider(meterProvider);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpGrpcLogExporter build() {
    return new OtlpGrpcLogExporter(delegate.build());
  }
}
