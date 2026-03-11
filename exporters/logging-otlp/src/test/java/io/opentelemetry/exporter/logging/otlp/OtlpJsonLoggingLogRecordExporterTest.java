/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.Level;

@SuppressLogger(OtlpJsonLoggingLogRecordExporter.class)
class OtlpJsonLoggingLogRecordExporterTest {

  private final TestDataExporter<LogRecordExporter> testDataExporter = TestDataExporter.forLogs();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpJsonLoggingLogRecordExporter.class);

  LogRecordExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = OtlpJsonLoggingLogRecordExporter.create();
  }

  @Test
  void log() throws Exception {
    testDataExporter.export(exporter);

    assertThat(logs.getEvents())
        .hasSize(1)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    String message = logs.getEvents().get(0).getMessage();
    String expectedJson = testDataExporter.getExpectedJson(false);
    JSONAssert.assertEquals("Got \n" + message, expectedJson, message, /* strict= */ false);
    assertThat(message).doesNotContain("\n");
  }

  @Test
  void logPrettyPrint() throws Exception {
    OtlpStdoutLogRecordExporter delegate =
        new OtlpStdoutLogRecordExporterBuilder(
                Logger.getLogger(OtlpJsonLoggingLogRecordExporter.class.getName()))
            .setWrapperJsonObject(false)
            .setPrettyPrint(true)
            .build();
    LogRecordExporter prettyExporter = new OtlpJsonLoggingLogRecordExporter(delegate);

    testDataExporter.export(prettyExporter);

    assertThat(logs.getEvents())
        .hasSize(1)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    String message = logs.getEvents().get(0).getMessage();
    String expectedJson = testDataExporter.getExpectedJson(false);
    JSONAssert.assertEquals("Got \n" + message, expectedJson, message, /* strict= */ false);
    assertThat(message).contains("\n");
  }

  @Test
  void shutdown() {
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(testDataExporter.export(exporter).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }
}
