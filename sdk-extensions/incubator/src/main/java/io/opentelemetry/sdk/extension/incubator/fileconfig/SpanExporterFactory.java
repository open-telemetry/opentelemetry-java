/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.logging.Logger;

final class SpanExporterFactory implements Factory<SpanExporterModel, SpanExporter> {

  private static final Logger logger = Logger.getLogger(SpanExporterFactory.class.getName());
  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(SpanExporterModel model, DeclarativeConfigContext context) {
    ConfigKeyValue spanExporterKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "span exporter");

    String exporterName = spanExporterKeyValue.getKey();
    SpanExporter exporter = context.loadComponent(SpanExporter.class, spanExporterKeyValue);

    // Apply customizers
    DeclarativeConfigurationBuilder builder = context.getBuilder();
    if (builder != null) {
      for (DeclarativeConfigurationBuilder.ExporterCustomizer<SpanExporter> customizerEntry :
          builder.getSpanExporterCustomizers()) {
        if (customizerEntry.getExporterType().isInstance(exporter)) {
          SpanExporter customized =
              customizerEntry.getCustomizer().apply(exporter, spanExporterKeyValue.getValue());
          if (customized == null) {
            logger.warning(
                "Span exporter customizer returned null for exporter: "
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
