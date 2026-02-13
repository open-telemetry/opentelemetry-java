/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.LogRecordProcessorComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorPropertyModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LogRecordProcessorFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          SpiHelper.create(LogRecordProcessorFactoryTest.class.getClassLoader()));

  @Test
  void create_BatchNullExporter() {
    assertThatThrownBy(
            () ->
                LogRecordProcessorFactory.getInstance()
                    .create(
                        new LogRecordProcessorModel().withBatch(new BatchLogRecordProcessorModel()),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("batch log record processor exporter is required but is null");
  }

  @Test
  void create_BatchDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    BatchLogRecordProcessor expectedProcessor =
        BatchLogRecordProcessor.builder(OtlpHttpLogRecordExporter.getDefault()).build();
    cleanup.addCloseable(expectedProcessor);

    LogRecordProcessor processor =
        LogRecordProcessorFactory.getInstance()
            .create(
                new LogRecordProcessorModel()
                    .withBatch(
                        new BatchLogRecordProcessorModel()
                            .withExporter(
                                new LogRecordExporterModel()
                                    .withOtlpHttp(new OtlpHttpExporterModel()))),
                context);
    cleanup.addCloseable(processor);
    cleanup.addCloseables(closeables);

    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  @Test
  void create_BatchConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    BatchLogRecordProcessor expectedProcessor =
        BatchLogRecordProcessor.builder(OtlpHttpLogRecordExporter.getDefault())
            .setScheduleDelay(Duration.ofMillis(1))
            .setMaxExportBatchSize(2)
            .setExporterTimeout(Duration.ofMillis(3))
            .build();
    cleanup.addCloseable(expectedProcessor);

    LogRecordProcessor processor =
        LogRecordProcessorFactory.getInstance()
            .create(
                new LogRecordProcessorModel()
                    .withBatch(
                        new BatchLogRecordProcessorModel()
                            .withExporter(
                                new LogRecordExporterModel()
                                    .withOtlpHttp(new OtlpHttpExporterModel()))
                            .withScheduleDelay(1)
                            .withMaxExportBatchSize(2)
                            .withExportTimeout(3)),
                context);
    cleanup.addCloseable(processor);
    cleanup.addCloseables(closeables);

    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  @Test
  void create_SimpleNullExporter() {
    assertThatThrownBy(
            () ->
                LogRecordProcessorFactory.getInstance()
                    .create(
                        new LogRecordProcessorModel()
                            .withSimple(new SimpleLogRecordProcessorModel()),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("simple log record processor exporter is required but is null");
  }

  @Test
  void create_SimpleConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    LogRecordProcessor expectedProcessor =
        SimpleLogRecordProcessor.create(OtlpHttpLogRecordExporter.getDefault());
    cleanup.addCloseable(expectedProcessor);

    LogRecordProcessor processor =
        LogRecordProcessorFactory.getInstance()
            .create(
                new LogRecordProcessorModel()
                    .withSimple(
                        new SimpleLogRecordProcessorModel()
                            .withExporter(
                                new LogRecordExporterModel()
                                    .withOtlpHttp(new OtlpHttpExporterModel()))),
                context);
    cleanup.addCloseable(processor);
    cleanup.addCloseables(closeables);

    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  @Test
  void create_SpiProcessor_Unknown() {
    assertThatThrownBy(
            () ->
                LogRecordProcessorFactory.getInstance()
                    .create(
                        new LogRecordProcessorModel()
                            .withAdditionalProperty(
                                "unknown_key",
                                new LogRecordProcessorPropertyModel()
                                    .withAdditionalProperty("key1", "value1")),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.logs.LogRecordProcessor with name \"unknown_key\".");
  }

  @Test
  void create_SpiExporter_Valid() {
    LogRecordProcessor logRecordProcessor =
        LogRecordProcessorFactory.getInstance()
            .create(
                new LogRecordProcessorModel()
                    .withAdditionalProperty(
                        "test",
                        new LogRecordProcessorPropertyModel()
                            .withAdditionalProperty("key1", "value1")),
                context);
    assertThat(logRecordProcessor)
        .isInstanceOf(LogRecordProcessorComponentProvider.TestLogRecordProcessor.class);
    Assertions.assertThat(
            ((LogRecordProcessorComponentProvider.TestLogRecordProcessor) logRecordProcessor)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
