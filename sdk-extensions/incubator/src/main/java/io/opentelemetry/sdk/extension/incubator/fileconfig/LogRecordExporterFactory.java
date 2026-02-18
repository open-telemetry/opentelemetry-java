/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.logging.Logger;

final class LogRecordExporterFactory implements Factory<LogRecordExporterModel, LogRecordExporter> {
  private static final Logger logger = Logger.getLogger(LogRecordExporterFactory.class.getName());
  private static final LogRecordExporterFactory INSTANCE = new LogRecordExporterFactory();

  private LogRecordExporterFactory() {}

  static LogRecordExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordExporter create(LogRecordExporterModel model, DeclarativeConfigContext context) {
    ConfigKeyValue logRecordExporterKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "log record exporter");

    String exporterName = logRecordExporterKeyValue.getKey();
    LogRecordExporter exporter =
        context.loadComponent(LogRecordExporter.class, logRecordExporterKeyValue);

    // Apply customizers
    DeclarativeConfigurationBuilder builder = context.getBuilder();
    if (builder != null) {
      for (DeclarativeConfigurationBuilder.ExporterCustomizer<LogRecordExporter> customizerEntry :
          builder.getLogRecordExporterCustomizers()) {
        if (customizerEntry.getExporterType().isInstance(exporter)) {
          LogRecordExporter customized =
              customizerEntry.getCustomizer().apply(exporter, logRecordExporterKeyValue.getValue());
          if (customized == null) {
            logger.warning(
                "Log record exporter customizer returned null for exporter: "
                    + exporterName
                    + ", using original exporter");
          } else {
            exporter = customized;
          }
        }
      }
    }
    return exporter;
  }
}
