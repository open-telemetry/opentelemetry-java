/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.internal.IncubatingExporterBuilderUtil;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * Declarative configuration SPI implementation for {@link OtlpStdoutLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutLogRecordExporterComponentProvider implements ComponentProvider {

  @Override
  public Class<LogRecordExporter> getType() {
    return LogRecordExporter.class;
  }

  @Override
  public String getName() {
    return "otlp_file/development";
  }

  @Override
  public LogRecordExporter create(DeclarativeConfigProperties config) {
    OtlpStdoutLogRecordExporterBuilder builder = OtlpStdoutLogRecordExporter.builder();
    IncubatingExporterBuilderUtil.configureExporterMemoryMode(config, builder::setMemoryMode);
    return builder.build();
  }
}
