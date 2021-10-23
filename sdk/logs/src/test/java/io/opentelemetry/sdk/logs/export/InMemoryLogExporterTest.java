/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static io.opentelemetry.sdk.logs.data.Severity.DEBUG;
import static io.opentelemetry.sdk.logs.util.TestUtil.createLog;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.logs.LogSink;
import io.opentelemetry.sdk.logs.LogSinkSdkProvider;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemoryLogExporter}. */
class InMemoryLogExporterTest {
  private final InMemoryLogExporter exporter = InMemoryLogExporter.create();

  private LogSinkSdkProvider logSinkSdkProvider;
  private LogSink logSink;

  @BeforeEach
  void setup() {
    logSinkSdkProvider =
        LogSinkSdkProvider.builder().addLogProcessor(SimpleLogProcessor.create(exporter)).build();
    logSink = logSinkSdkProvider.get(null, null);
  }

  @AfterEach
  void tearDown() {
    logSinkSdkProvider.shutdown();
  }

  @Test
  void getFinishedLogItems() {
    logSink.offer(createLog(DEBUG, "message 1"));
    logSink.offer(createLog(DEBUG, "message 2"));
    logSink.offer(createLog(DEBUG, "message 3"));

    List<LogData> logItems = exporter.getFinishedLogItems();
    assertThat(logItems).isNotNull();
    assertThat(logItems.size()).isEqualTo(3);
    assertThat(logItems.get(0).getBody().asString()).isEqualTo("message 1");
    assertThat(logItems.get(1).getBody().asString()).isEqualTo("message 2");
    assertThat(logItems.get(2).getBody().asString()).isEqualTo("message 3");
  }

  @Test
  void reset() {
    logSink.offer(createLog(DEBUG, "message 1"));
    logSink.offer(createLog(DEBUG, "message 2"));
    logSink.offer(createLog(DEBUG, "message 3"));
    List<LogData> logItems = exporter.getFinishedLogItems();
    assertThat(logItems).isNotNull();
    assertThat(logItems.size()).isEqualTo(3);
    // Reset then expect no items in memory.
    exporter.reset();
    assertThat(exporter.getFinishedLogItems()).isEmpty();
  }

  @Test
  void shutdown() {
    logSink.offer(createLog(DEBUG, "message 1"));
    logSink.offer(createLog(DEBUG, "message 2"));
    logSink.offer(createLog(DEBUG, "message 3"));
    List<LogData> logItems = exporter.getFinishedLogItems();
    assertThat(logItems).isNotNull();
    assertThat(logItems.size()).isEqualTo(3);
    // Shutdown then expect no items in memory.
    exporter.shutdown();
    assertThat(exporter.getFinishedLogItems()).isEmpty();
    // Cannot add new elements after the shutdown.
    logSink.offer(createLog(DEBUG, "message 1"));
    assertThat(exporter.getFinishedLogItems()).isEmpty();
  }

  @Test
  void export_ReturnCode() {
    LogRecord logRecord = createLog(DEBUG, "message 1");
    assertThat(exporter.export(Collections.singletonList(logRecord)).isSuccess()).isTrue();
    exporter.shutdown();
    // After shutdown no more export.
    assertThat(exporter.export(Collections.singletonList(logRecord)).isSuccess()).isFalse();
    exporter.reset();
    // Reset does not do anything if already shutdown.
    assertThat(exporter.export(Collections.singletonList(logRecord)).isSuccess()).isFalse();
  }
}
