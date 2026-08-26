/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil.setLoggerConfigurator;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeLimitsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LoggerProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SeverityNumberModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalLoggerConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalLoggerConfiguratorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalLoggerMatcherAndConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.LoggerProviderModelAccessor;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoggerProviderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private static final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          ComponentLoader.forClassLoader(LoggerProviderFactoryTest.class.getClassLoader()));

  @BeforeEach
  void setup() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
  }

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(LoggerProviderAndAttributeLimits model, SdkLoggerProvider expectedProvider) {
    List<Closeable> closeables = new ArrayList<>();
    cleanup.addCloseable(expectedProvider);

    SdkLoggerProvider provider = LoggerProviderFactory.getInstance().create(model, context).build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.argumentSet(
            "null limits",
            LoggerProviderAndAttributeLimits.create(null, null),
            SdkLoggerProvider.builder().build()),
        Arguments.argumentSet(
            "empty models",
            LoggerProviderAndAttributeLimits.create(
                new AttributeLimitsModel(), new LoggerProviderModel()),
            SdkLoggerProvider.builder().build()),
        Arguments.argumentSet(
            "full configuration",
            LoggerProviderAndAttributeLimits.create(
                new AttributeLimitsModel(),
                LoggerProviderModelAccessor.withLoggerConfigurator(
                    new LoggerProviderModel()
                        .withLimits(
                            new LogRecordLimitsModel()
                                .withAttributeCountLimit(1)
                                .withAttributeValueLengthLimit(2))
                        .withProcessors(
                            Collections.singletonList(
                                new LogRecordProcessorModel()
                                    .withBatch(
                                        new BatchLogRecordProcessorModel()
                                            .withExporter(
                                                new LogRecordExporterModel()
                                                    .withOtlpHttp(new OtlpHttpExporterModel()))))),
                    new ExperimentalLoggerConfiguratorModel()
                        .withDefaultConfig(new ExperimentalLoggerConfigModel().withEnabled(false))
                        .withLoggers(
                            Collections.singletonList(
                                new ExperimentalLoggerMatcherAndConfigModel()
                                    .withName("foo")
                                    .withConfig(
                                        new ExperimentalLoggerConfigModel()
                                            .withEnabled(true)
                                            .withTraceBased(true)
                                            .withMinimumSeverity(SeverityNumberModel.INFO)))))),
            setLoggerConfigurator(
                    SdkLoggerProvider.builder(),
                    ScopeConfigurator.<LoggerConfig>builder()
                        .setDefault(LoggerConfig.disabled())
                        .addCondition(
                            ScopeConfiguratorBuilder.nameMatchesGlob("foo"),
                            LoggerConfig.builder()
                                .setEnabled(true)
                                .setTraceBased(true)
                                .setMinimumSeverity(Severity.INFO)
                                .build())
                        .build())
                .setLogLimits(
                    () ->
                        LogLimits.builder()
                            .setMaxNumberOfAttributes(1)
                            .setMaxAttributeValueLength(2)
                            .build())
                .addLogRecordProcessor(
                    BatchLogRecordProcessor.builder(
                            OtlpHttpLogRecordExporter.builder().setComponentLoader(context).build())
                        .build())
                .build()));
  }

  @ParameterizedTest
  @MethodSource("severityNumberArguments")
  void severityNumber(SeverityNumberModel model, Severity expectedSeverity) {
    assertThat(LoggerProviderFactory.severityNumberToSeverity(model)).isEqualTo(expectedSeverity);
  }

  private static Stream<Arguments> severityNumberArguments() {
    return Stream.of(
        Arguments.argumentSet("TRACE", SeverityNumberModel.TRACE, Severity.TRACE),
        Arguments.argumentSet("TRACE_2", SeverityNumberModel.TRACE_2, Severity.TRACE2),
        Arguments.argumentSet("TRACE_3", SeverityNumberModel.TRACE_3, Severity.TRACE3),
        Arguments.argumentSet("TRACE_4", SeverityNumberModel.TRACE_4, Severity.TRACE4),
        Arguments.argumentSet("DEBUG", SeverityNumberModel.DEBUG, Severity.DEBUG),
        Arguments.argumentSet("DEBUG_2", SeverityNumberModel.DEBUG_2, Severity.DEBUG2),
        Arguments.argumentSet("DEBUG_3", SeverityNumberModel.DEBUG_3, Severity.DEBUG3),
        Arguments.argumentSet("DEBUG_4", SeverityNumberModel.DEBUG_4, Severity.DEBUG4),
        Arguments.argumentSet("INFO", SeverityNumberModel.INFO, Severity.INFO),
        Arguments.argumentSet("INFO_2", SeverityNumberModel.INFO_2, Severity.INFO2),
        Arguments.argumentSet("INFO_3", SeverityNumberModel.INFO_3, Severity.INFO3),
        Arguments.argumentSet("INFO_4", SeverityNumberModel.INFO_4, Severity.INFO4),
        Arguments.argumentSet("WARN", SeverityNumberModel.WARN, Severity.WARN),
        Arguments.argumentSet("WARN_2", SeverityNumberModel.WARN_2, Severity.WARN2),
        Arguments.argumentSet("WARN_3", SeverityNumberModel.WARN_3, Severity.WARN3),
        Arguments.argumentSet("WARN_4", SeverityNumberModel.WARN_4, Severity.WARN4),
        Arguments.argumentSet("ERROR", SeverityNumberModel.ERROR, Severity.ERROR),
        Arguments.argumentSet("ERROR_2", SeverityNumberModel.ERROR_2, Severity.ERROR2),
        Arguments.argumentSet("ERROR_3", SeverityNumberModel.ERROR_3, Severity.ERROR3),
        Arguments.argumentSet("ERROR_4", SeverityNumberModel.ERROR_4, Severity.ERROR4),
        Arguments.argumentSet("FATAL", SeverityNumberModel.FATAL, Severity.FATAL),
        Arguments.argumentSet("FATAL_2", SeverityNumberModel.FATAL_2, Severity.FATAL2),
        Arguments.argumentSet("FATAL_3", SeverityNumberModel.FATAL_3, Severity.FATAL3),
        Arguments.argumentSet("FATAL_4", SeverityNumberModel.FATAL_4, Severity.FATAL4));
  }
}
