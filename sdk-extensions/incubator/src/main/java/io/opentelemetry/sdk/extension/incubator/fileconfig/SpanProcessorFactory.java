/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Map;

final class SpanProcessorFactory implements Factory<SpanProcessorModel, SpanProcessor> {

  private static final SpanProcessorFactory INSTANCE = new SpanProcessorFactory();

  private SpanProcessorFactory() {}

  static SpanProcessorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanProcessor create(
      SpanProcessorModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    BatchSpanProcessorModel batchModel = model.getBatch();
    if (batchModel != null) {
      SpanExporterModel exporterModel =
          FileConfigUtil.requireNonNull(batchModel.getExporter(), "batch span processor exporter");
      SpanExporter spanExporter =
          SpanExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
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
      return FileConfigUtil.addAndReturn(closeables, builder.build());
    }

    SimpleSpanProcessorModel simpleModel = model.getSimple();
    if (simpleModel != null) {
      SpanExporterModel exporterModel =
          FileConfigUtil.requireNonNull(
              simpleModel.getExporter(), "simple span processor exporter");
      SpanExporter spanExporter =
          SpanExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      return FileConfigUtil.addAndReturn(closeables, SimpleSpanProcessor.create(spanExporter));
    }

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "span processor");
    SpanProcessor spanProcessor =
        FileConfigUtil.loadComponent(
            spiHelper, SpanProcessor.class, keyValue.getKey(), keyValue.getValue());
    return FileConfigUtil.addAndReturn(closeables, spanProcessor);
  }
}
