/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_TRACES;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * {@link SpanExporter} SPI implementation for {@link OtlpGrpcSpanExporter} and {@link
 * OtlpHttpSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpSpanExporterProvider
    implements ConfigurableSpanExporterProvider, AutoConfigureListener {

  private final AtomicReference<MeterProvider> meterProviderRef =
      new AtomicReference<>(MeterProvider.noop());

  @Override
  public SpanExporter createExporter(ConfigProperties config) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_TRACES, config);
    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      return createHttpProtobufExporter(config, c -> {});
    } else if (protocol.equals(PROTOCOL_GRPC)) {
      return createGrpcExporter(config, c -> {});
    }
    throw new ConfigurationException("Unsupported OTLP traces protocol: " + protocol);
  }

  protected SpanExporter createHttpProtobufExporter(ConfigProperties config, Consumer<OtlpHttpSpanExporterBuilder> configurator) {
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
        builder::setRetryPolicy);
    builder.setMeterProvider(meterProviderRef::get);

    configurator.accept(builder);

    return builder.build();
  }

  protected SpanExporter createGrpcExporter(ConfigProperties config, Consumer<OtlpGrpcSpanExporterBuilder> configurator) {
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
        builder::setRetryPolicy);
    builder.setMeterProvider(meterProviderRef::get);

    configurator.accept(builder);

    return builder.build();
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

  @Override
  public void afterAutoConfigure(OpenTelemetrySdk sdk) {
    meterProviderRef.set(sdk.getMeterProvider());
  }
}
