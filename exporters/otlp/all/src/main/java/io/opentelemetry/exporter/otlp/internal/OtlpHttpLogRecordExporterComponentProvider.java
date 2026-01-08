/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_LOGS;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpHttpLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpHttpLogRecordExporterComponentProvider implements ComponentProvider {

  @Override
  public Class<LogRecordExporter> getType() {
    return LogRecordExporter.class;
  }

  @Override
  public String getName() {
    return "otlp_http";
  }

  @Override
  public LogRecordExporter create(DeclarativeConfigProperties config) {
    OtlpHttpLogRecordExporterBuilder builder = httpBuilder();

    OtlpDeclarativeConfigUtil.configureOtlpExporterBuilder(
        DATA_TYPE_LOGS,
        config,
        builder::setComponentLoader,
        builder::setEndpoint,
        builder::addHeader,
        builder::setCompression,
        builder::setTimeout,
        builder::setTrustedCertificates,
        builder::setClientTls,
        builder::setRetryPolicy,
        builder::setMemoryMode,
        /* isHttpProtobuf= */ true);

    return builder.build();
  }

  // Visible for testing
  OtlpHttpLogRecordExporterBuilder httpBuilder() {
    return OtlpHttpLogRecordExporter.builder();
  }
}
