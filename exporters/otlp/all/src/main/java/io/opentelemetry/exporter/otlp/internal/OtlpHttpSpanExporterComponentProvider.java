/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_TRACES;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpHttpSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpHttpSpanExporterComponentProvider implements ComponentProvider {

  @Override
  public Class<SpanExporter> getType() {
    return SpanExporter.class;
  }

  @Override
  public String getName() {
    return "otlp_http";
  }

  @Override
  public SpanExporter create(DeclarativeConfigProperties config) {
    OtlpHttpSpanExporterBuilder builder = httpBuilder();

    OtlpDeclarativeConfigUtil.configureOtlpExporterBuilder(
        DATA_TYPE_TRACES,
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
  OtlpHttpSpanExporterBuilder httpBuilder() {
    return OtlpHttpSpanExporter.builder();
  }
}
