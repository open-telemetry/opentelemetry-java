/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.component.LogRecordProcessorComponentProvider;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordProcessorPropertyModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.logs.EventToSpanEventBridge;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LogRecordProcessorFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private static final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          ComponentLoader.forClassLoader(LogRecordProcessorFactoryTest.class.getClassLoader()));

  @BeforeEach
  void setup() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
  }

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(LogRecordProcessorModel model, LogRecordProcessor expectedProcessor) {
    cleanup.addCloseable(expectedProcessor);
    LogRecordProcessor processor = LogRecordProcessorFactory.getInstance().create(model, context);
    cleanup.addCloseable(processor);
    assertThat(processor.toString()).isEqualTo(expectedProcessor.toString());
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.of(
            new LogRecordProcessorModel()
                .withBatch(
                    new BatchLogRecordProcessorModel()
                        .withExporter(
                            new LogRecordExporterModel()
                                .withOtlpHttp(new OtlpHttpExporterModel()))),
            BatchLogRecordProcessor.builder(
                    OtlpHttpLogRecordExporter.builder().setComponentLoader(context).build())
                .build()),
        Arguments.of(
            new LogRecordProcessorModel()
                .withBatch(
                    new BatchLogRecordProcessorModel()
                        .withExporter(
                            new LogRecordExporterModel().withOtlpHttp(new OtlpHttpExporterModel()))
                        .withScheduleDelay(1)
                        .withMaxExportBatchSize(2)
                        .withExportTimeout(3)),
            BatchLogRecordProcessor.builder(
                    OtlpHttpLogRecordExporter.builder().setComponentLoader(context).build())
                .setScheduleDelay(Duration.ofMillis(1))
                .setMaxExportBatchSize(2)
                .setExporterTimeout(Duration.ofMillis(3))
                .build()),
        Arguments.of(
            new LogRecordProcessorModel()
                .withSimple(
                    new SimpleLogRecordProcessorModel()
                        .withExporter(
                            new LogRecordExporterModel()
                                .withOtlpHttp(new OtlpHttpExporterModel()))),
            SimpleLogRecordProcessor.create(
                OtlpHttpLogRecordExporter.builder().setComponentLoader(context).build())),
        Arguments.of(
            new LogRecordProcessorModel()
                .withAdditionalProperty(
                    "event_to_span_event_bridge/development",
                    new LogRecordProcessorPropertyModel()),
            EventToSpanEventBridge.create()),
        Arguments.of(
            new LogRecordProcessorModel()
                .withAdditionalProperty("test", new LogRecordProcessorPropertyModel()),
            LogRecordProcessorComponentProvider.TestLogRecordProcessor.create()));
  }

  @ParameterizedTest
  @MethodSource("createInvalidTestCases")
  void create_Invalid(LogRecordProcessorModel model, String expectedMessage) {
    assertThatThrownBy(() -> LogRecordProcessorFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  private static Stream<Arguments> createInvalidTestCases() {
    return Stream.of(
        Arguments.of(
            new LogRecordProcessorModel().withBatch(new BatchLogRecordProcessorModel()),
            "batch log record processor exporter is required but is null"),
        Arguments.of(
            new LogRecordProcessorModel().withSimple(new SimpleLogRecordProcessorModel()),
            "simple log record processor exporter is required but is null"),
        Arguments.of(
            new LogRecordProcessorModel()
                .withAdditionalProperty(
                    "unknown_key",
                    new LogRecordProcessorPropertyModel().withAdditionalProperty("key1", "value1")),
            "No component provider detected for io.opentelemetry.sdk.logs.LogRecordProcessor with name \"unknown_key\"."));
  }
}
