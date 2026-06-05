/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.component.SpanProcessorComponentProvider;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SimpleSpanProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanProcessorPropertyModel;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SpanProcessorFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private static final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          ComponentLoader.forClassLoader(SpanProcessorFactoryTest.class.getClassLoader()));

  @BeforeEach
  void setup() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
  }

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(SpanProcessorModel model, SpanProcessor expectedProcessor) {
    cleanup.addCloseable(expectedProcessor);
    SpanProcessor processor = SpanProcessorFactory.getInstance().create(model, context);
    cleanup.addCloseable(processor);
    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.of(
            new SpanProcessorModel()
                .withBatch(
                    new BatchSpanProcessorModel()
                        .withExporter(
                            new SpanExporterModel().withOtlpHttp(new OtlpHttpExporterModel()))),
            BatchSpanProcessor.builder(
                    OtlpHttpSpanExporter.builder().setComponentLoader(context).build())
                .build()),
        Arguments.of(
            new SpanProcessorModel()
                .withBatch(
                    new BatchSpanProcessorModel()
                        .withExporter(
                            new SpanExporterModel().withOtlpHttp(new OtlpHttpExporterModel()))
                        .withScheduleDelay(1)
                        .withMaxExportBatchSize(2)
                        .withExportTimeout(3)),
            BatchSpanProcessor.builder(
                    OtlpHttpSpanExporter.builder().setComponentLoader(context).build())
                .setScheduleDelay(Duration.ofMillis(1))
                .setMaxExportBatchSize(2)
                .setExporterTimeout(Duration.ofMillis(3))
                .build()),
        Arguments.of(
            new SpanProcessorModel()
                .withSimple(
                    new SimpleSpanProcessorModel()
                        .withExporter(
                            new SpanExporterModel().withOtlpHttp(new OtlpHttpExporterModel()))),
            SimpleSpanProcessor.create(
                OtlpHttpSpanExporter.builder().setComponentLoader(context).build())));
  }

  @ParameterizedTest
  @MethodSource("createInvalidTestCases")
  void create_Invalid(SpanProcessorModel model, String expectedMessage) {
    assertThatThrownBy(() -> SpanProcessorFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  private static Stream<Arguments> createInvalidTestCases() {
    return Stream.of(
        Arguments.of(
            new SpanProcessorModel().withBatch(new BatchSpanProcessorModel()),
            "batch span processor exporter is required but is null"),
        Arguments.of(
            new SpanProcessorModel().withSimple(new SimpleSpanProcessorModel()),
            "simple span processor exporter is required but is null"),
        Arguments.of(
            new SpanProcessorModel()
                .withAdditionalProperty(
                    "unknown_key",
                    new SpanProcessorPropertyModel().withAdditionalProperty("key1", "value1")),
            "No component provider detected for io.opentelemetry.sdk.trace.SpanProcessor with name \"unknown_key\"."));
  }

  @Test
  void create_SpiProcessor_Valid() {
    SpanProcessor spanProcessor =
        SpanProcessorFactory.getInstance()
            .create(
                new SpanProcessorModel()
                    .withAdditionalProperty(
                        "test",
                        new SpanProcessorPropertyModel().withAdditionalProperty("key1", "value1")),
                context);
    assertThat(spanProcessor).isInstanceOf(SpanProcessorComponentProvider.TestSpanProcessor.class);
    assertThat(
            ((SpanProcessorComponentProvider.TestSpanProcessor) spanProcessor)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
