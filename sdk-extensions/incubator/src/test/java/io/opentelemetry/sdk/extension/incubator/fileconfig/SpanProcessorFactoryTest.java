/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessor;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SpanProcessorFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(SpanProcessorFactoryTest.class.getClassLoader());

  @Test
  void create_BatchNullExporter() {
    assertThatThrownBy(
            () ->
                SpanProcessorFactory.getInstance()
                    .create(
                        new SpanProcessor().withBatch(new BatchSpanProcessor()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("batch span processor exporter is required but is null");
  }

  @Test
  void create_BatchDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.trace.export.BatchSpanProcessor expectedProcessor =
        io.opentelemetry.sdk.trace.export.BatchSpanProcessor.builder(
                OtlpGrpcSpanExporter.getDefault())
            .build();
    cleanup.addCloseable(expectedProcessor);

    io.opentelemetry.sdk.trace.SpanProcessor processor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessor()
                    .withBatch(
                        new BatchSpanProcessor()
                            .withExporter(new SpanExporter().withOtlp(new Otlp()))),
                spiHelper,
                closeables);
    cleanup.addCloseable(processor);
    cleanup.addCloseables(closeables);

    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  @Test
  void create_BatchConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.trace.export.BatchSpanProcessor expectedProcessor =
        io.opentelemetry.sdk.trace.export.BatchSpanProcessor.builder(
                OtlpGrpcSpanExporter.getDefault())
            .setScheduleDelay(Duration.ofMillis(1))
            .setMaxExportBatchSize(2)
            .setExporterTimeout(Duration.ofMillis(3))
            .build();
    cleanup.addCloseable(expectedProcessor);

    io.opentelemetry.sdk.trace.SpanProcessor processor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessor()
                    .withBatch(
                        new BatchSpanProcessor()
                            .withExporter(new SpanExporter().withOtlp(new Otlp()))
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
                SpanProcessorFactory.getInstance()
                    .create(
                        new SpanProcessor().withSimple(new SimpleSpanProcessor()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("simple span processor exporter is required but is null");
  }

  @Test
  void create_SimpleConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.trace.SpanProcessor expectedProcessor =
        io.opentelemetry.sdk.trace.export.SimpleSpanProcessor.create(
            OtlpGrpcSpanExporter.getDefault());
    cleanup.addCloseable(expectedProcessor);

    io.opentelemetry.sdk.trace.SpanProcessor processor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessor()
                    .withSimple(
                        new SimpleSpanProcessor()
                            .withExporter(new SpanExporter().withOtlp(new Otlp()))),
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
                SpanProcessorFactory.getInstance()
                    .create(
                        new SpanProcessor()
                            .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        closeables))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("Unrecognized span processor(s): [test]");
    cleanup.addCloseables(closeables);
  }
}
