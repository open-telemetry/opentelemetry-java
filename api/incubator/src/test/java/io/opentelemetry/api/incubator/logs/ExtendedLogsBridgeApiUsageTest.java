/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.logs.internal.LoggerConfig.disabled;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.AnyValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.AnyValueBody;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
    if (loggerA.isEnabled()) {
      loggerA
          .logRecordBuilder()
          .setBody("hello world!")
          .setAllAttributes(Attributes.builder().put("result", flipCoin()).build())
          .emit();
    }
    if (loggerB.isEnabled()) {
      loggerB
          .logRecordBuilder()
          .setBody("hello world!")
          .setAllAttributes(Attributes.builder().put("result", flipCoin()).build())
          .emit();
    }

    // loggerA is enabled, loggerB is disabled
    assertThat(loggerA.isEnabled()).isTrue();
    assertThat(loggerB.isEnabled()).isFalse();

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

  @Test
  void extendedLogRecordBuilderUsage() {
    // Setup SdkLoggerProvider
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // Simple processor w/ in-memory exporter used for demonstration purposes
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();

    // Get a Logger for a scope
    Logger logger = loggerProvider.get("org.foo.my-scope");

    // Cast to ExtendedLogRecordBuilder, and emit a log
    ((ExtendedLogRecordBuilder) logger.logRecordBuilder())
        // ...can set AnyValue log record body, allowing for arbitrarily complex data
        .setBody(
            AnyValue.of(
                ImmutableMap.of(
                    "key1",
                    AnyValue.of("value1"),
                    "key2",
                    AnyValue.of(
                        ImmutableMap.of(
                            "childKey1",
                            AnyValue.of("value2"),
                            "childKey2",
                            AnyValue.of("value3"))))))
        .emit();

    // SDK can access AnyValue body by casting to AnyValueBody
    loggerProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            logData ->
                assertThat(((AnyValueBody) logData.getBody()).asAnyValue())
                    .isEqualTo(
                        AnyValue.of(
                            ImmutableMap.of(
                                "key1",
                                AnyValue.of("value1"),
                                "key2",
                                AnyValue.of(
                                    ImmutableMap.of(
                                        "childKey1",
                                        AnyValue.of("value2"),
                                        "childKey2",
                                        AnyValue.of("value3")))))));
  }
}
