/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.SpanProcessorComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
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
                        new SpanProcessorModel().withBatch(new BatchSpanProcessorModel()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(ConfigurationException.class)
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
                new SpanProcessorModel()
                    .withBatch(
                        new BatchSpanProcessorModel()
                            .withExporter(new SpanExporterModel().withOtlp(new OtlpModel()))),
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
                new SpanProcessorModel()
                    .withBatch(
                        new BatchSpanProcessorModel()
                            .withExporter(new SpanExporterModel().withOtlp(new OtlpModel()))
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
                        new SpanProcessorModel().withSimple(new SimpleSpanProcessorModel()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(ConfigurationException.class)
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
                new SpanProcessorModel()
                    .withSimple(
                        new SimpleSpanProcessorModel()
                            .withExporter(new SpanExporterModel().withOtlp(new OtlpModel()))),
                spiHelper,
                closeables);
    cleanup.addCloseable(processor);
    cleanup.addCloseables(closeables);

    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  @Test
  void create_SpiProcessor_Unknown() {
    assertThatThrownBy(
            () ->
                SpanProcessorFactory.getInstance()
                    .create(
                        new SpanProcessorModel()
                            .withAdditionalProperty(
                                "unknown_key", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.trace.SpanProcessor with name \"unknown_key\".");
  }

  @Test
  void create_SpiExporter_Valid() {
    io.opentelemetry.sdk.trace.SpanProcessor spanProcessor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessorModel()
                    .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                spiHelper,
                new ArrayList<>());
    assertThat(spanProcessor).isInstanceOf(SpanProcessorComponentProvider.TestSpanProcessor.class);
    Assertions.assertThat(
            ((SpanProcessorComponentProvider.TestSpanProcessor) spanProcessor)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
