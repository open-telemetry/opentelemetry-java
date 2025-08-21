/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

/**
 * Declarative configuration SPI implementation for {@link SystemOutLogRecordExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ConsoleLogRecordExporterComponentProvider
    implements ComponentProvider<LogRecordExporter> {

  @Override
  public Class<LogRecordExporter> getType() {
    return LogRecordExporter.class;
  }

  @Override
  public String getName() {
    return "console";
  }

  @Override
  public LogRecordExporter create(DeclarativeConfigProperties config, ComponentProviderLoader componentProviderLoader) {
    return SystemOutLogRecordExporter.create();
  }
}
