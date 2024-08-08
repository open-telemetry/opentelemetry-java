/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.otlp.stream.trace.StdoutSpanExporter;
import io.opentelemetry.exporter.otlp.stream.trace.StdoutSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * File configuration SPI implementation for {@link
 * StdoutSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class StdoutSpanExporterComponentProvider
    implements ComponentProvider<SpanExporter> {

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
    StdoutSpanExporterBuilder builder = StdoutSpanExporter.builder();
    ExporterBuilderUtil.configureExporterMemoryMode(config, builder::setMemoryMode);
    return builder.build();
  }
}
