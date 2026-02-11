/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_TRACES;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ExtendedComponentProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpGrpcSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpGrpcSpanExporterComponentProvider implements ExtendedComponentProvider {

  @Override
  public Class<SpanExporter> getType() {
    return SpanExporter.class;
  }

  @Override
  public String getName() {
    return "otlp_grpc";
  }

  @Override
  public SpanExporter create(DeclarativeConfigProperties config, ConfigProvider configProvider) {
    OtlpGrpcSpanExporterBuilder builder = grpcBuilder();

    OtlpDeclarativeConfigUtil.configureOtlpExporterBuilder(
        DATA_TYPE_TRACES,
        config,
        configProvider,
        builder::setComponentLoader,
        builder::setEndpoint,
        builder::addHeader,
        builder::setCompression,
        builder::setTimeout,
        builder::setTrustedCertificates,
        builder::setClientTls,
        builder::setRetryPolicy,
        builder::setMemoryMode,
        /* isHttpProtobuf= */ false,
        builder::setInternalTelemetryVersion,
        () -> builder.setMeterProvider(MeterProvider::noop));

    return builder.build();
  }

  // Visible for testing
  OtlpGrpcSpanExporterBuilder grpcBuilder() {
    return OtlpGrpcSpanExporter.builder();
  }
}
