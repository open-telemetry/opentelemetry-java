/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.logs.internal.LoggerConfig.disabled;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of extended Logs Bridge API. */
class ExtendedLogsBridgeApiUsageTest {

  @Test
  void loggerEnabled() {
    // Setup SdkLoggerProvider
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProviderBuilder loggerProviderBuilder =
        SdkLoggerProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // In-memory exporter used for demonstration purposes
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter));
    // Disable loggerB
    SdkLoggerProviderUtil.addLoggerConfiguratorCondition(
        loggerProviderBuilder, nameEquals("loggerB"), disabled());
    SdkLoggerProvider loggerProvider = loggerProviderBuilder.build();

    // Create loggerA and loggerB
    ExtendedLogger loggerA = (ExtendedLogger) loggerProvider.get("loggerA");
    ExtendedLogger loggerB = (ExtendedLogger) loggerProvider.get("loggerB");

    // Check if logger is enabled before emitting log and avoid unnecessary computation
    if (loggerA.isEnabled(Severity.INFO)) {
      loggerA
          .logRecordBuilder()
          .setSeverity(Severity.INFO)
          .setBody("hello world!")
          .setAllAttributes(Attributes.builder().put("result", flipCoin()).build())
          .emit();
    }
    if (loggerB.isEnabled(Severity.INFO)) {
      loggerB
          .logRecordBuilder()
          .setSeverity(Severity.INFO)
          .setBody("hello world!")
          .setAllAttributes(Attributes.builder().put("result", flipCoin()).build())
          .emit();
    }

    // loggerA is enabled, loggerB is disabled
    assertThat(loggerA.isEnabled(Severity.INFO)).isTrue();
    assertThat(loggerB.isEnabled(Severity.INFO)).isFalse();

    // Collected data only consists of logs from loggerA. Note, loggerB's logs would be
    // omitted from the results even if logs were emitted. The check if enabled simply avoids
    // unnecessary computation.
    assertThat(exporter.getFinishedLogRecordItems())
        .allSatisfy(
            logRecordData ->
                assertThat(logRecordData.getInstrumentationScopeInfo().getName())
                    .isEqualTo("loggerA"));
  }

  private static final Random random = new Random();

  private static String flipCoin() {
    return random.nextBoolean() ? "heads" : "tails";
  }
}
