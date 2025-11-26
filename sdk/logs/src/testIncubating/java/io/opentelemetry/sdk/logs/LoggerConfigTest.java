/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameMatchesGlob;
import static io.opentelemetry.sdk.logs.internal.LoggerConfig.defaultConfig;
import static io.opentelemetry.sdk.logs.internal.LoggerConfig.disabled;
import static io.opentelemetry.sdk.logs.internal.LoggerConfig.enabled;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoggerConfigTest {

  @Test
  void builder_AllFields() {
    LoggerConfig config =
        LoggerConfig.builder()
            .setEnabled(false)
            .setMinimumSeverity(Severity.WARN)
            .setTraceBased(true)
            .build();

    assertThat(config.isEnabled()).isFalse();
    assertThat(config.getMinimumSeverity()).isEqualTo(Severity.WARN);
    assertThat(config.isTraceBased()).isTrue();
  }

  @Test
  void builder_Defaults() {
    LoggerConfig config = LoggerConfig.builder().build();

    assertThat(config.isEnabled()).isTrue();
    assertThat(config.getMinimumSeverity()).isEqualTo(Severity.UNDEFINED_SEVERITY_NUMBER);
    assertThat(config.isTraceBased()).isFalse();
  }

  @Test
  void disableScopes() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            // Disable loggerB. Since loggers are enabled by default, loggerA and loggerC are
            // enabled.
            .addLoggerConfiguratorCondition(nameEquals("loggerB"), disabled())
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();

    Logger loggerA = loggerProvider.get("loggerA");
    Logger loggerB = loggerProvider.get("loggerB");
    Logger loggerC = loggerProvider.get("loggerC");

    loggerA.logRecordBuilder().setBody("messageA").emit();
    loggerB.logRecordBuilder().setBody("messageB").emit();
    loggerC.logRecordBuilder().setBody("messageC").emit();

    // Only logs from loggerA and loggerC should be seen
    assertThat(exporter.getFinishedLogRecordItems())
        .satisfies(
            metrics -> {
              Map<InstrumentationScopeInfo, List<LogRecordData>> logsByScope =
                  metrics.stream()
                      .collect(Collectors.groupingBy(LogRecordData::getInstrumentationScopeInfo));
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("loggerA"))).hasSize(1);
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("loggerB"))).isNull();
              assertThat(logsByScope.get(InstrumentationScopeInfo.create("loggerC"))).hasSize(1);
            });
    // loggerA and loggerC are enabled, loggerB is disabled.
    assertThat(((ExtendedLogger) loggerA).isEnabled(Severity.INFO)).isTrue();
    assertThat(((ExtendedLogger) loggerB).isEnabled(Severity.INFO)).isFalse();
    assertThat(((ExtendedLogger) loggerC).isEnabled(Severity.INFO)).isTrue();
  }

  @ParameterizedTest
  @MethodSource("loggerConfiguratorArgs")
  void loggerConfigurator(
      ScopeConfigurator<LoggerConfig> loggerConfigurator,
      InstrumentationScopeInfo scope,
      LoggerConfig expectedLoggerConfig) {
    LoggerConfig loggerConfig = loggerConfigurator.apply(scope);
    loggerConfig = loggerConfig == null ? defaultConfig() : loggerConfig;
    assertThat(loggerConfig).isEqualTo(expectedLoggerConfig);
  }

  private static final InstrumentationScopeInfo scopeCat = InstrumentationScopeInfo.create("cat");
  private static final InstrumentationScopeInfo scopeDog = InstrumentationScopeInfo.create("dog");
  private static final InstrumentationScopeInfo scopeDuck = InstrumentationScopeInfo.create("duck");

  private static Stream<Arguments> loggerConfiguratorArgs() {
    ScopeConfigurator<LoggerConfig> defaultConfigurator =
        LoggerConfig.configuratorBuilder().build();
    ScopeConfigurator<LoggerConfig> disableCat =
        LoggerConfig.configuratorBuilder()
            .addCondition(nameEquals("cat"), disabled())
            // Second matching rule for cat should be ignored
            .addCondition(nameEquals("cat"), enabled())
            .build();
    ScopeConfigurator<LoggerConfig> disableStartsWithD =
        LoggerConfig.configuratorBuilder().addCondition(nameMatchesGlob("d*"), disabled()).build();
    ScopeConfigurator<LoggerConfig> enableCat =
        LoggerConfig.configuratorBuilder()
            .setDefault(disabled())
            .addCondition(nameEquals("cat"), enabled())
            // Second matching rule for cat should be ignored
            .addCondition(nameEquals("cat"), disabled())
            .build();
    ScopeConfigurator<LoggerConfig> enableStartsWithD =
        LoggerConfig.configuratorBuilder()
            .setDefault(disabled())
            .addCondition(nameMatchesGlob("d*"), enabled())
            .build();

    return Stream.of(
        // default
        Arguments.of(defaultConfigurator, scopeCat, defaultConfig()),
        Arguments.of(defaultConfigurator, scopeDog, defaultConfig()),
        Arguments.of(defaultConfigurator, scopeDuck, defaultConfig()),
        // default enabled, disable cat
        Arguments.of(disableCat, scopeCat, disabled()),
        Arguments.of(disableCat, scopeDog, enabled()),
        Arguments.of(disableCat, scopeDuck, enabled()),
        // default enabled, disable pattern
        Arguments.of(disableStartsWithD, scopeCat, enabled()),
        Arguments.of(disableStartsWithD, scopeDog, disabled()),
        Arguments.of(disableStartsWithD, scopeDuck, disabled()),
        // default disabled, enable cat
        Arguments.of(enableCat, scopeCat, enabled()),
        Arguments.of(enableCat, scopeDog, disabled()),
        Arguments.of(enableCat, scopeDuck, disabled()),
        // default disabled, enable pattern
        Arguments.of(enableStartsWithD, scopeCat, disabled()),
        Arguments.of(enableStartsWithD, scopeDog, enabled()),
        Arguments.of(enableStartsWithD, scopeDuck, enabled()));
  }

  @Test
  void setScopeConfigurator() {
    // 1. Initially, configure all loggers to be enabled except loggerB
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLoggerConfiguratorCondition(nameEquals("loggerB"), disabled())
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();

    ExtendedSdkLogger loggerA = (ExtendedSdkLogger) loggerProvider.get("loggerA");
    ExtendedSdkLogger loggerB = (ExtendedSdkLogger) loggerProvider.get("loggerB");
    ExtendedSdkLogger loggerC = (ExtendedSdkLogger) loggerProvider.get("loggerC");

    // verify isEnabled()
    assertThat(loggerA.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isTrue();
    assertThat(loggerB.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isFalse();
    assertThat(loggerC.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isTrue();

    // verify logs are emitted as expected
    loggerA.logRecordBuilder().setBody("logA").emit();
    loggerB.logRecordBuilder().setBody("logB").emit();
    loggerC.logRecordBuilder().setBody("logC").emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactlyInAnyOrder(
            log -> assertThat(log).hasBody("logA"), log -> assertThat(log).hasBody("logC"));
    exporter.reset();

    // 2. Update config to disable all loggers
    loggerProvider.setLoggerConfigurator(
        ScopeConfigurator.<LoggerConfig>builder().setDefault(disabled()).build());

    // verify isEnabled()
    assertThat(loggerA.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isFalse();
    assertThat(loggerB.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isFalse();
    assertThat(loggerC.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isFalse();

    // verify logs are emitted as expected
    loggerA.logRecordBuilder().setBody("logA").emit();
    loggerB.logRecordBuilder().setBody("logB").emit();
    loggerC.logRecordBuilder().setBody("logC").emit();
    assertThat(exporter.getFinishedLogRecordItems()).isEmpty();

    // 3. Update config to restore original
    loggerProvider.setLoggerConfigurator(
        ScopeConfigurator.<LoggerConfig>builder()
            .addCondition(nameEquals("loggerB"), disabled())
            .build());

    // verify isEnabled()
    assertThat(loggerA.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isTrue();
    assertThat(loggerB.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isFalse();
    assertThat(loggerC.isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER, Context.current())).isTrue();

    // verify logs are emitted as expected
    loggerA.logRecordBuilder().setBody("logA").emit();
    loggerB.logRecordBuilder().setBody("logB").emit();
    loggerC.logRecordBuilder().setBody("logC").emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            log -> assertThat(log).hasBody("logA"), log -> assertThat(log).hasBody("logC"));
  }
}
