/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static io.opentelemetry.api.logs.Severity.DEBUG;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.testing.assertj.LogAssertions;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemoryLogRecordExporter}. */
class InMemoryLogRecordExporterTest {
  private final InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();

  private SdkLoggerProvider loggerProvider;
  private Logger logger;

  @BeforeEach
  void setup() {
    loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();
    logger = loggerProvider.loggerBuilder("logger").build();
  }

  @AfterEach
  void tearDown() {
    loggerProvider.shutdown();
  }

  @Test
  void getFinishedLogItems() {
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 1").emit();
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 2").emit();
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 3").emit();

    List<LogRecordData> logItems = exporter.getFinishedLogItems();
    assertThat(logItems).isNotNull();
    assertThat(logItems.size()).isEqualTo(3);
    LogAssertions.assertThat(logItems.get(0)).hasBody("message 1");
    LogAssertions.assertThat(logItems.get(1)).hasBody("message 2");
    LogAssertions.assertThat(logItems.get(2)).hasBody("message 3");
  }

  @Test
  void reset() {
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 1").emit();
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 2").emit();
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 3").emit();
    List<LogRecordData> logItems = exporter.getFinishedLogItems();
    assertThat(logItems).isNotNull();
    assertThat(logItems.size()).isEqualTo(3);
    // Reset then expect no items in memory.
    exporter.reset();
    assertThat(exporter.getFinishedLogItems()).isEmpty();
  }

  @Test
  void shutdown() {
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 1").emit();
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 2").emit();
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 3").emit();
    List<LogRecordData> logItems = exporter.getFinishedLogItems();
    assertThat(logItems).isNotNull();
    assertThat(logItems.size()).isEqualTo(3);
    // Shutdown then expect no items in memory.
    exporter.shutdown();
    assertThat(exporter.getFinishedLogItems()).isEmpty();
    // Cannot add new elements after the shutdown.
    logger.logRecordBuilder().setSeverity(DEBUG).setBody("message 1").emit();
    assertThat(exporter.getFinishedLogItems()).isEmpty();
  }

  @Test
  void export_ReturnCode() {
    LogRecordData logRecordData =
        TestLogRecordData.builder().setBody("message 1").setSeverity(DEBUG).build();
    assertThat(exporter.export(Collections.singletonList(logRecordData)).isSuccess()).isTrue();
    exporter.shutdown();
    // After shutdown no more export.
    assertThat(exporter.export(Collections.singletonList(logRecordData)).isSuccess()).isFalse();
    exporter.reset();
    // Reset does not do anything if already shutdown.
    assertThat(exporter.export(Collections.singletonList(logRecordData)).isSuccess()).isFalse();
  }
}
