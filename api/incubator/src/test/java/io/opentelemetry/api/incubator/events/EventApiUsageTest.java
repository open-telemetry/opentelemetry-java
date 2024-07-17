/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.AnyValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of Event API. */
class EventApiUsageTest {

  @Test
  void eventApiUsage() {
    // Setup SdkEventLoggerProvider, which delegates to SdkLoggerProvider
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // Simple processor w/ in-memory exporter used for demonstration purposes
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();
    EventLoggerProvider eventLoggerProvider = SdkEventLoggerProvider.create(loggerProvider);

    // Get an EventLogger for a scope
    EventLogger eventLogger = eventLoggerProvider.get("org.foo.my-scope");

    // Emit an event
    eventLogger
        .builder("org.foo.my-event")
        // Add fields to the payload. The API has helpers for adding field values which are
        // primitives or arrays of primitives, but you can also add a field with type AnyValue,
        // allowing for arbitrarily complex payloads.
        .put("key1", "value1")
        .put(
            "key2",
            AnyValue.of(
                ImmutableMap.of(
                    "childKey1", AnyValue.of("value2"), "childKey2", AnyValue.of("value3"))))
        // Optionally set other fields, including timestamp, severity, context, and attributes
        // (attributes provide additional details about the event which are not part of the well
        // defined payload)
        .emit();

    // Events manifest as log records with an event.name attribute, and with the payload fields in
    // the AnyValue log record body
    loggerProvider.forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            logData -> {
              assertThat(logData)
                  .hasAttributes(
                      Attributes.builder().put("event.name", "org.foo.my-event").build());
              assertThat(logData.getAnyValueBody())
                  .isNotNull()
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
                                      AnyValue.of("value3"))))));
            });
  }
}
