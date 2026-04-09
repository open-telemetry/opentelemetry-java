/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;

final class SpanExporterFactory implements Factory<SpanExporterModel, SpanExporter> {

  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(SpanExporterModel model, DeclarativeConfigContext context) {
    ConfigKeyValue spanExporterKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "span exporter");
    SpanExporter exporter = context.loadComponent(SpanExporter.class, spanExporterKeyValue);
    for (DeclarativeConfigurationBuilder.Customizer<SpanExporter> customizer :
        context.getBuilder().getSpanExporterCustomizers()) {
      exporter =
          customizer.maybeCustomize(
              exporter, spanExporterKeyValue.getKey(), spanExporterKeyValue.getValue());
    }
    return exporter;
  }
}
