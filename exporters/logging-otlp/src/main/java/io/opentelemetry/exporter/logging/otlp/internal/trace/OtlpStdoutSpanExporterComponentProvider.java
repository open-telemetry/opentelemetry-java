/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.trace;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * File configuration SPI implementation for {@link OtlpStdoutSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpStdoutSpanExporterComponentProvider implements ComponentProvider<SpanExporter> {

  @Override
  public Class<SpanExporter> getType() {
    return SpanExporter.class;
  }

  @Override
  public String getName() {
    return "otlp-stdout";
  }

  @Override
  public SpanExporter create(StructuredConfigProperties config) {
    OtlpJsonLoggingSpanExporterBuilder builder = OtlpStdoutSpanExporter.builder();
    ExporterBuilderUtil.configureExporterMemoryMode(config, builder::setMemoryMode);
    return builder.build();
  }
}
