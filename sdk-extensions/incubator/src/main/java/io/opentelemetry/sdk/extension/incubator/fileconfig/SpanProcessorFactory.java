/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class SpanProcessorFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessor,
        SpanProcessor> {

  private static final SpanProcessorFactory INSTANCE = new SpanProcessorFactory();

  private SpanProcessorFactory() {}

  static SpanProcessorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanProcessor create(
      @Nullable
          io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessor model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return SpanProcessor.composite();
    }

    io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessor
        batchModel = model.getBatch();
    if (batchModel != null) {
      SpanExporter exporterModel = batchModel.getExporter();
      io.opentelemetry.sdk.trace.export.SpanExporter spanExporter =
          SpanExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      if (spanExporter == null) {
        throw new ConfigurationException("exporter required for batch span processor");
      }
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

    io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessor
        simpleModel = model.getSimple();
    if (simpleModel != null) {
      SpanExporter exporterModel = simpleModel.getExporter();
      io.opentelemetry.sdk.trace.export.SpanExporter spanExporter =
          SpanExporterFactory.getInstance().create(exporterModel, spiHelper, closeables);
      if (spanExporter == null) {
        throw new ConfigurationException("exporter required for simple span processor");
      }
      return FileConfigUtil.addAndReturn(closeables, SimpleSpanProcessor.create(spanExporter));
    }

    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new ConfigurationException(
            "Invalid configuration - multiple span processors set: "
                + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
      }
      Map.Entry<String, Object> processorKeyValue =
          additionalProperties.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException("Missing processor. This is a programming error."));
      SpanProcessor spanProcessor =
          FileConfigUtil.loadComponent(
              spiHelper,
              SpanProcessor.class,
              processorKeyValue.getKey(),
              processorKeyValue.getValue());
      return FileConfigUtil.addAndReturn(closeables, spanProcessor);
    }

    return SpanProcessor.composite();
  }
}
