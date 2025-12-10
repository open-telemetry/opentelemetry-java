/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNullResource;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Map;

final class SpanExporterFactory implements Factory<SpanExporterModel, SpanExporter> {

  private static final String RESOURCE_NAME = "span exporter";

  private static final SpanExporterFactory INSTANCE = new SpanExporterFactory();

  private SpanExporterFactory() {}

  static SpanExporterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanExporter create(SpanExporterModel model, DeclarativeConfigContext context) {

    String key = null;
    Object resource = null;

    if (model.getOtlpHttp() != null) {
      key = "otlp_http";
      resource = model.getOtlpHttp();
    }
    if (model.getOtlpGrpc() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "otlp_grpc";
      resource = model.getOtlpGrpc();
    }
    if (model.getOtlpFileDevelopment() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "otlp_file/development";
      resource = model.getOtlpFileDevelopment();
    }
    if (model.getConsole() != null) {
      requireNullResource(resource, RESOURCE_NAME, model.getAdditionalProperties());
      key = "console";
      resource = model.getConsole();
    }
    if (key == null || resource == null) {
      Map.Entry<String, ?> keyValue =
          FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), RESOURCE_NAME);
      key = keyValue.getKey();
      resource = keyValue.getValue();
    }
    // TODO: remove after merging
    // https://github.com/open-telemetry/opentelemetry-configuration/pull/460
    if ("zipkin".equals(key)) {
      return SpanExporter.composite();
    }

    SpanExporter spanExporter = context.loadComponent(SpanExporter.class, key, resource);
    return context.addCloseable(spanExporter);
  }
}
