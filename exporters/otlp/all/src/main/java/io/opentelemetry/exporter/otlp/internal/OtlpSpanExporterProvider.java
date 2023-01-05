/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.DATA_TYPE_TRACES;
import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * {@link SpanExporter} SPI implementation for {@link OtlpGrpcSpanExporter} and {@link
 * OtlpHttpSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpSpanExporterProvider implements ConfigurableSpanExporterProvider {
  @Override
  public SpanExporter createExporter(ConfigProperties config) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_TRACES, config);
    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      OtlpHttpSpanExporterBuilder builder = httpBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_TRACES,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates,
          builder::setClientTls,
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));

      return builder.build();
    } else if (protocol.equals(PROTOCOL_GRPC)) {
      OtlpGrpcSpanExporterBuilder builder = grpcBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_TRACES,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates,
          builder::setClientTls,
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));

      return builder.build();
    }
    throw new ConfigurationException("Unsupported OTLP traces protocol: " + protocol);
  }

  @Override
  public String getName() {
    return "otlp";
  }

  // Visible for testing
  OtlpHttpSpanExporterBuilder httpBuilder() {
    return OtlpHttpSpanExporter.builder();
  }

  // Visible for testing
  OtlpGrpcSpanExporterBuilder grpcBuilder() {
    return OtlpGrpcSpanExporter.builder();
  }
}
