/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.SpanProcessorComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorPropertyModel;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SpanProcessorFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          SpiHelper.create(SpanProcessorFactoryTest.class.getClassLoader()));

  @Test
  void create_BatchNullExporter() {
    assertThatThrownBy(
            () ->
                SpanProcessorFactory.getInstance()
                    .create(
                        new SpanProcessorModel().withBatch(new BatchSpanProcessorModel()), context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("batch span processor exporter is required but is null");
  }

  @Test
  void create_BatchDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    BatchSpanProcessor expectedProcessor =
        BatchSpanProcessor.builder(OtlpHttpSpanExporter.getDefault()).build();
    cleanup.addCloseable(expectedProcessor);

    SpanProcessor processor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessorModel()
                    .withBatch(
                        new BatchSpanProcessorModel()
                            .withExporter(
                                new SpanExporterModel().withOtlpHttp(new OtlpHttpExporterModel()))),
                context);
    cleanup.addCloseable(processor);
    cleanup.addCloseables(closeables);

    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  @Test
  void create_BatchConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    BatchSpanProcessor expectedProcessor =
        BatchSpanProcessor.builder(OtlpHttpSpanExporter.getDefault())
            .setScheduleDelay(Duration.ofMillis(1))
            .setMaxExportBatchSize(2)
            .setExporterTimeout(Duration.ofMillis(3))
            .build();
    cleanup.addCloseable(expectedProcessor);

    SpanProcessor processor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessorModel()
                    .withBatch(
                        new BatchSpanProcessorModel()
                            .withExporter(
                                new SpanExporterModel().withOtlpHttp(new OtlpHttpExporterModel()))
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
                SpanProcessorFactory.getInstance()
                    .create(
                        new SpanProcessorModel().withSimple(new SimpleSpanProcessorModel()),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("simple span processor exporter is required but is null");
  }

  @Test
  void create_SimpleConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    SpanProcessor expectedProcessor = SimpleSpanProcessor.create(OtlpHttpSpanExporter.getDefault());
    cleanup.addCloseable(expectedProcessor);

    SpanProcessor processor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessorModel()
                    .withSimple(
                        new SimpleSpanProcessorModel()
                            .withExporter(
                                new SpanExporterModel().withOtlpHttp(new OtlpHttpExporterModel()))),
                context);
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
                                "unknown_key",
                                new SpanProcessorPropertyModel()
                                    .withAdditionalProperty("key1", "value1")),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.trace.SpanProcessor with name \"unknown_key\".");
  }

  @Test
  void create_SpiExporter_Valid() {
    SpanProcessor spanProcessor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessorModel()
                    .withAdditionalProperty(
                        "test",
                        new SpanProcessorPropertyModel().withAdditionalProperty("key1", "value1")),
                context);
    assertThat(spanProcessor).isInstanceOf(SpanProcessorComponentProvider.TestSpanProcessor.class);
    Assertions.assertThat(
            ((SpanProcessorComponentProvider.TestSpanProcessor) spanProcessor)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
