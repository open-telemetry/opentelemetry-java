/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.AnyValueBody;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of extended Logs Bridge API. */
class ExtendedLogsBridgeApiUsageTest {

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
