/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Map;

final class SpanExporterFactory implements Factory<SpanExporterModel, SpanExporter> {

  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(SpanExporterModel model, DeclarativeConfigContext context) {
    Map.Entry<String, DeclarativeConfigProperties> spanExporterKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "span exporter");
    SpanExporter spanExporter =
        context.loadComponent(
            SpanExporter.class, spanExporterKeyValue.getKey(), spanExporterKeyValue.getValue());
    return context.addCloseable(spanExporter);
  }
}
