/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessor;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LogRecordProcessorFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(LogRecordProcessorFactoryTest.class.getClassLoader());

  @Test
  void create_BatchNullExporter() {
    assertThatThrownBy(
            () ->
                LogRecordProcessorFactory.getInstance()
                    .create(
                        new LogRecordProcessor().withBatch(new BatchLogRecordProcessor()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("batch log record processor exporter is required but is null");
  }

  @Test
  void create_BatchDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor expectedProcessor =
        io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor.builder(
                OtlpGrpcLogRecordExporter.getDefault())
            .build();
    cleanup.addCloseable(expectedProcessor);

    io.opentelemetry.sdk.logs.LogRecordProcessor processor =
        LogRecordProcessorFactory.getInstance()
            .create(
                new LogRecordProcessor()
                    .withBatch(
                        new BatchLogRecordProcessor()
                            .withExporter(new LogRecordExporter().withOtlp(new Otlp()))),
                spiHelper,
                closeables);
    cleanup.addCloseable(processor);
    cleanup.addCloseables(closeables);

    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  @Test
  void create_BatchConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor expectedProcessor =
        io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor.builder(
                OtlpGrpcLogRecordExporter.getDefault())
            .setScheduleDelay(Duration.ofMillis(1))
            .setMaxExportBatchSize(2)
            .setExporterTimeout(Duration.ofMillis(3))
            .build();
    cleanup.addCloseable(expectedProcessor);

    io.opentelemetry.sdk.logs.LogRecordProcessor processor =
        LogRecordProcessorFactory.getInstance()
            .create(
                new LogRecordProcessor()
                    .withBatch(
                        new BatchLogRecordProcessor()
                            .withExporter(new LogRecordExporter().withOtlp(new Otlp()))
                            .withScheduleDelay(1)
                            .withMaxExportBatchSize(2)
                            .withExportTimeout(3)),
                spiHelper,
                closeables);
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
                        new LogRecordProcessor().withSimple(new SimpleLogRecordProcessor()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("simple log record processor exporter is required but is null");
  }

  @Test
  void create_SimpleConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.logs.LogRecordProcessor expectedProcessor =
        io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor.create(
            OtlpGrpcLogRecordExporter.getDefault());
    cleanup.addCloseable(expectedProcessor);

    io.opentelemetry.sdk.logs.LogRecordProcessor processor =
        LogRecordProcessorFactory.getInstance()
            .create(
                new LogRecordProcessor()
                    .withSimple(
                        new SimpleLogRecordProcessor()
                            .withExporter(new LogRecordExporter().withOtlp(new Otlp()))),
                spiHelper,
                closeables);
    cleanup.addCloseable(processor);
    cleanup.addCloseables(closeables);

    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  @Test
  void create_SpiProcessor() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                LogRecordProcessorFactory.getInstance()
                    .create(
                        new LogRecordProcessor()
                            .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        closeables))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("Unrecognized log record processor(s): [test]");
    cleanup.addCloseables(closeables);
  }
}
