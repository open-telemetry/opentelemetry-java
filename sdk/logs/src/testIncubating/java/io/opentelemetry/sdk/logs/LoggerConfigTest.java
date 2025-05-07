/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameMatchesGlob;
import static io.opentelemetry.sdk.logs.internal.LoggerConfig.defaultConfig;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.logs.Logger;
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
  void disableScopes() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            // Disable loggerB. Since loggers are enabled by default, loggerA and loggerC are
            // enabled.
            .addLoggerConfiguratorCondition(nameEquals("loggerB"), LoggerConfig.disabled())
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
    assertThat(((ExtendedLogger) loggerA).isEnabled()).isTrue();
    assertThat(((ExtendedLogger) loggerB).isEnabled()).isFalse();
    assertThat(((ExtendedLogger) loggerC).isEnabled()).isTrue();
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
            .addCondition(nameEquals("cat"), LoggerConfig.disabled())
            // Second matching rule for cat should be ignored
            .addCondition(nameEquals("cat"), LoggerConfig.enabled())
            .build();
    ScopeConfigurator<LoggerConfig> disableStartsWithD =
        LoggerConfig.configuratorBuilder()
            .addCondition(nameMatchesGlob("d*"), LoggerConfig.disabled())
            .build();
    ScopeConfigurator<LoggerConfig> enableCat =
        LoggerConfig.configuratorBuilder()
            .setDefault(LoggerConfig.disabled())
            .addCondition(nameEquals("cat"), LoggerConfig.enabled())
            // Second matching rule for cat should be ignored
            .addCondition(nameEquals("cat"), LoggerConfig.disabled())
            .build();
    ScopeConfigurator<LoggerConfig> enableStartsWithD =
        LoggerConfig.configuratorBuilder()
            .setDefault(LoggerConfig.disabled())
            .addCondition(nameMatchesGlob("d*"), LoggerConfig.enabled())
            .build();

    return Stream.of(
        // default
        Arguments.of(defaultConfigurator, scopeCat, defaultConfig()),
        Arguments.of(defaultConfigurator, scopeDog, defaultConfig()),
        Arguments.of(defaultConfigurator, scopeDuck, defaultConfig()),
        // default enabled, disable cat
        Arguments.of(disableCat, scopeCat, LoggerConfig.disabled()),
        Arguments.of(disableCat, scopeDog, LoggerConfig.enabled()),
        Arguments.of(disableCat, scopeDuck, LoggerConfig.enabled()),
        // default enabled, disable pattern
        Arguments.of(disableStartsWithD, scopeCat, LoggerConfig.enabled()),
        Arguments.of(disableStartsWithD, scopeDog, LoggerConfig.disabled()),
        Arguments.of(disableStartsWithD, scopeDuck, LoggerConfig.disabled()),
        // default disabled, enable cat
        Arguments.of(enableCat, scopeCat, LoggerConfig.enabled()),
        Arguments.of(enableCat, scopeDog, LoggerConfig.disabled()),
        Arguments.of(enableCat, scopeDuck, LoggerConfig.disabled()),
        // default disabled, enable pattern
        Arguments.of(enableStartsWithD, scopeCat, LoggerConfig.disabled()),
        Arguments.of(enableStartsWithD, scopeDog, LoggerConfig.enabled()),
        Arguments.of(enableStartsWithD, scopeDuck, LoggerConfig.enabled()));
  }

  @Test
  void setScopeConfigurator() {
    // 1. Initially, configure all loggers to be enabled except loggerB
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLoggerConfiguratorCondition(nameEquals("loggerB"), LoggerConfig.disabled())
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();

    ExtendedSdkLogger loggerA = (ExtendedSdkLogger) loggerProvider.get("loggerA");
    ExtendedSdkLogger loggerB = (ExtendedSdkLogger) loggerProvider.get("loggerB");
    ExtendedSdkLogger loggerC = (ExtendedSdkLogger) loggerProvider.get("loggerC");

    // verify isEnabled()
    assertThat(loggerA.isEnabled()).isTrue();
    assertThat(loggerB.isEnabled()).isFalse();
    assertThat(loggerC.isEnabled()).isTrue();

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
        ScopeConfigurator.<LoggerConfig>builder().setDefault(LoggerConfig.disabled()).build());

    // verify isEnabled()
    assertThat(loggerA.isEnabled()).isFalse();
    assertThat(loggerB.isEnabled()).isFalse();
    assertThat(loggerC.isEnabled()).isFalse();

    // verify logs are emitted as expected
    loggerA.logRecordBuilder().setBody("logA").emit();
    loggerB.logRecordBuilder().setBody("logB").emit();
    loggerC.logRecordBuilder().setBody("logC").emit();
    assertThat(exporter.getFinishedLogRecordItems()).isEmpty();

    // 3. Update config to restore original
    loggerProvider.setLoggerConfigurator(
        ScopeConfigurator.<LoggerConfig>builder()
            .addCondition(nameEquals("loggerB"), LoggerConfig.disabled())
            .build());

    // verify isEnabled()
    assertThat(loggerA.isEnabled()).isTrue();
    assertThat(loggerB.isEnabled()).isFalse();
    assertThat(loggerC.isEnabled()).isTrue();

    // verify logs are emitted as expected
    loggerA.logRecordBuilder().setBody("logA").emit();
    loggerB.logRecordBuilder().setBody("logB").emit();
    loggerC.logRecordBuilder().setBody("logC").emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            log -> assertThat(log).hasBody("logA"), log -> assertThat(log).hasBody("logC"));
  }
}
