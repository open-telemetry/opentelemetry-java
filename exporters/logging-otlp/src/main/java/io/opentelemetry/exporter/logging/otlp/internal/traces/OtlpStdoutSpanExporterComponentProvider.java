/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.traces;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpStdoutSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutSpanExporterComponentProvider
    implements ComponentProvider<SpanExporter> {

  @Override
  public Class<SpanExporter> getType() {
    return SpanExporter.class;
  }

  @Override
  public String getName() {
    return "experimental-otlp/stdout";
  }

  @Override
  public SpanExporter create(StructuredConfigProperties config) {
    OtlpStdoutSpanExporterBuilder builder = OtlpStdoutSpanExporter.builder();
    ExporterBuilderUtil.configureExporterMemoryMode(config, builder::setMemoryMode);
    return builder.build();
  }
}
