/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil.setLoggerConfigurator;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel.SeverityNumber;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoggerProviderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          SpiHelper.create(LoggerProviderFactoryTest.class.getClassLoader()));

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
        Arguments.of(
            LoggerProviderAndAttributeLimits.create(null, null),
            SdkLoggerProvider.builder().build()),
        Arguments.of(
            LoggerProviderAndAttributeLimits.create(
                new AttributeLimitsModel(), new LoggerProviderModel()),
            SdkLoggerProvider.builder().build()),
        Arguments.of(
            LoggerProviderAndAttributeLimits.create(
                new AttributeLimitsModel(),
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
                                                .withOtlpHttp(new OtlpHttpExporterModel())))))
                    .withLoggerConfiguratorDevelopment(
                        new ExperimentalLoggerConfiguratorModel()
                            .withDefaultConfig(
                                new ExperimentalLoggerConfigModel().withDisabled(true))
                            .withLoggers(
                                Collections.singletonList(
                                    new ExperimentalLoggerMatcherAndConfigModel()
                                        .withName("foo")
                                        .withConfig(
                                            new ExperimentalLoggerConfigModel()
                                                .withDisabled(false)
                                                .withTraceBased(true)
                                                .withMinimumSeverity(SeverityNumber.INFO)))))),
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
                    io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor.builder(
                            OtlpHttpLogRecordExporter.getDefault())
                        .build())
                .build()));
  }

  @ParameterizedTest
  @MethodSource("severityNumberArguments")
  void severityNumber(SeverityNumber model, Severity expectedSeverity) {
    assertThat(LoggerProviderFactory.SeverityNumberFactory.INSTANCE.create(model, context))
        .isEqualTo(expectedSeverity);
  }

  private static Stream<Arguments> severityNumberArguments() {
    return Stream.of(
        Arguments.of(SeverityNumber.TRACE, Severity.TRACE),
        Arguments.of(SeverityNumber.TRACE_2, Severity.TRACE2),
        Arguments.of(SeverityNumber.TRACE_3, Severity.TRACE3),
        Arguments.of(SeverityNumber.TRACE_4, Severity.TRACE4),
        Arguments.of(SeverityNumber.DEBUG, Severity.DEBUG),
        Arguments.of(SeverityNumber.DEBUG_2, Severity.DEBUG2),
        Arguments.of(SeverityNumber.DEBUG_3, Severity.DEBUG3),
        Arguments.of(SeverityNumber.DEBUG_4, Severity.DEBUG4),
        Arguments.of(SeverityNumber.INFO, Severity.INFO),
        Arguments.of(SeverityNumber.INFO_2, Severity.INFO2),
        Arguments.of(SeverityNumber.INFO_3, Severity.INFO3),
        Arguments.of(SeverityNumber.INFO_4, Severity.INFO4),
        Arguments.of(SeverityNumber.WARN, Severity.WARN),
        Arguments.of(SeverityNumber.WARN_2, Severity.WARN2),
        Arguments.of(SeverityNumber.WARN_3, Severity.WARN3),
        Arguments.of(SeverityNumber.WARN_4, Severity.WARN4),
        Arguments.of(SeverityNumber.ERROR, Severity.ERROR),
        Arguments.of(SeverityNumber.ERROR_2, Severity.ERROR2),
        Arguments.of(SeverityNumber.ERROR_3, Severity.ERROR3),
        Arguments.of(SeverityNumber.ERROR_4, Severity.ERROR4),
        Arguments.of(SeverityNumber.FATAL, Severity.FATAL),
        Arguments.of(SeverityNumber.FATAL_2, Severity.FATAL2),
        Arguments.of(SeverityNumber.FATAL_3, Severity.FATAL3),
        Arguments.of(SeverityNumber.FATAL_4, Severity.FATAL4));
  }
}
