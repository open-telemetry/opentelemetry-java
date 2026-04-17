/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;

final class SpanProcessorFactory implements Factory<SpanProcessorModel, SpanProcessor> {

  private static final SpanProcessorFactory INSTANCE = new SpanProcessorFactory();

  private SpanProcessorFactory() {}

  static SpanProcessorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanProcessor create(SpanProcessorModel model, DeclarativeConfigContext context) {
    // We don't use the variable till later but call validate first to confirm there are not
    // multiple samplers.
    ConfigKeyValue processorKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "span processor");

    if (model.getBatch() != null) {
      return createBatchLogRecordProcessor(model.getBatch(), context);
    }
    if (model.getSimple() != null) {
      return createSimpleLogRecordProcessor(model.getSimple(), context);
    }

    return context.loadComponent(SpanProcessor.class, processorKeyValue);
  }

  private static SpanProcessor createBatchLogRecordProcessor(
      BatchSpanProcessorModel batchModel, DeclarativeConfigContext context) {
    SpanExporterModel exporterModel =
        FileConfigUtil.requireNonNull(batchModel.getExporter(), "batch span processor exporter");
    SpanExporter spanExporter = SpanExporterFactory.getInstance().create(exporterModel, context);
    BatchSpanProcessorBuilder builder = BatchSpanProcessor.builder(spanExporter);
    if (batchModel.getExportTimeout() != null) {
      builder.setExporterTimeout(Duration.ofMillis(batchModel.getExportTimeout()));
    }
    if (batchModel.getMaxExportBatchSize() != null) {
      builder.setMaxExportBatchSize(batchModel.getMaxExportBatchSize());
    }
    if (batchModel.getMaxQueueSize() != null) {
      builder.setMaxQueueSize(batchModel.getMaxQueueSize());
    }
    if (batchModel.getScheduleDelay() != null) {
      builder.setScheduleDelay(Duration.ofMillis(batchModel.getScheduleDelay()));
    }
    context.setInternalTelemetry(builder::setMeterProvider, builder::setInternalTelemetryVersion);

    return context.addCloseable(builder.build());
  }

  private static SpanProcessor createSimpleLogRecordProcessor(
      SimpleSpanProcessorModel simpleModel, DeclarativeConfigContext context) {
    SpanExporterModel exporterModel =
        FileConfigUtil.requireNonNull(simpleModel.getExporter(), "simple span processor exporter");
    SpanExporter spanExporter = SpanExporterFactory.getInstance().create(exporterModel, context);
    SimpleSpanProcessorBuilder builder = SimpleSpanProcessor.builder(spanExporter);
    context.setInternalTelemetry(builder::setMeterProvider);
    return context.addCloseable(builder.build());
  }
}
