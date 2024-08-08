/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.otlp.stream.logs.StdoutLogRecordExporter;
import io.opentelemetry.exporter.otlp.stream.logs.StdoutLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * File configuration SPI implementation for {@link
 * io.opentelemetry.exporter.otlp.stream.logs.StdoutLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class StdoutLogRecordExporterComponentProvider
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
    StdoutLogRecordExporterBuilder builder = StdoutLogRecordExporter.builder();
    ExporterBuilderUtil.configureExporterMemoryMode(config, builder::setMemoryMode);
    return builder.build();
  }
}
