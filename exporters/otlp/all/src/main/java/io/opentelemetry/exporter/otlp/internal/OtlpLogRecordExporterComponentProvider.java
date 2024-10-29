/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_LOGS;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpHttpLogRecordExporter} and {@link
 * OtlpGrpcLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpLogRecordExporterComponentProvider
    implements ComponentProvider<LogRecordExporter> {

  @Override
  public Class<LogRecordExporter> getType() {
    return LogRecordExporter.class;
  }

  @Override
  public String getName() {
    return "otlp";
  }

  @Override
  public LogRecordExporter create(StructuredConfigProperties config) {
    String protocol = OtlpConfigUtil.getStructuredConfigOtlpProtocol(config);

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
          builder::setRetryPolicy,
          builder::setMemoryMode);

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
          builder::setRetryPolicy,
          builder::setMemoryMode);

      return builder.build();
    }
    throw new ConfigurationException("Unsupported OTLP metrics protocol: " + protocol);
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
