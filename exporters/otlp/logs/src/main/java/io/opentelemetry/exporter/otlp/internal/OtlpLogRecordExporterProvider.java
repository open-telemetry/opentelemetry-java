/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.DATA_TYPE_LOGS;
import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * {@link LogRecordExporter} SPI implementation for {@link OtlpGrpcLogRecordExporter} and {@link
 * OtlpHttpLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpLogRecordExporterProvider implements ConfigurableLogRecordExporterProvider {
  @Override
  public LogRecordExporter createExporter(ConfigProperties config) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_LOGS, config);

    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      OtlpHttpLogRecordExporterBuilder builder = httpBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_LOGS,
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
      OtlpGrpcLogRecordExporterBuilder builder = grpcBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_LOGS,
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
    throw new ConfigurationException("Unsupported OTLP logs protocol: " + protocol);
  }

  @Override
  public String getName() {
    return "otlp";
  }

  // Visible for testing
  OtlpHttpLogRecordExporterBuilder httpBuilder() {
    return OtlpHttpLogRecordExporter.builder();
  }

  // Visible for testing
  OtlpGrpcLogRecordExporterBuilder grpcBuilder() {
    return OtlpGrpcLogRecordExporter.builder();
  }
}
