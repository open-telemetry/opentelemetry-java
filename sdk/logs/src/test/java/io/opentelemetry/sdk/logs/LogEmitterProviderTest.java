/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.logs.data.ReadableLogData;
import io.opentelemetry.sdk.logs.data.ReadableLogRecord;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.util.TestLogExporter;
import io.opentelemetry.sdk.logs.util.TestLogProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LogEmitterProviderTest {

  private static final String INSTRUMENTATION_LIBRARY =
      LogEmitterProviderTest.class.getSimpleName();
  private static final String INSTRUMENTATION_LIBRARY_VERSION = "0.1";
  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(INSTRUMENTATION_LIBRARY, INSTRUMENTATION_LIBRARY_VERSION);

  private static LogRecord createLogRecord(Severity severity, String message) {
    return ReadableLogRecord.builder()
        .setEpochMillis(System.currentTimeMillis())
        .setTraceId(TraceId.getInvalid())
        .setSpanId(SpanId.getInvalid())
        .setFlags(TraceFlags.getDefault().asByte())
        .setSeverity(severity)
        .setSeverityText("really severe")
        .setName("log1")
        .setBody(message)
        .setAttributes(Attributes.builder().put("animal", "cat").build())
        .build();
  }

  private static LogData toLogData(LogRecord logRecord) {
    return ReadableLogData.create(RESOURCE, INSTRUMENTATION_LIBRARY_INFO, logRecord);
  }

  @Test
  void testLogEmitterProvider() {
    TestLogExporter exporter = new TestLogExporter();
    LogProcessor processor = BatchLogProcessor.builder(exporter).build();
    LogEmitterProvider provider =
        LogEmitterProvider.builder().setResource(RESOURCE).addLogProcessor(processor).build();
    LogEmitter emitter = provider.get(INSTRUMENTATION_LIBRARY, INSTRUMENTATION_LIBRARY_VERSION);
    LogRecord logRecord = createLogRecord(Severity.ERROR, "test");
    emitter.emit(logRecord);
    provider.forceFlush().join(500, TimeUnit.MILLISECONDS);
    List<LogData> records = exporter.getRecords();
    assertThat(records).singleElement().isEqualTo(toLogData(logRecord));
  }

  @Test
  void testBatchSize() {
    TestLogExporter exporter = new TestLogExporter();
    LogProcessor processor =
        BatchLogProcessor.builder(exporter)
            .setScheduleDelayMillis(10000) // Long enough to not be in play
            .setMaxExportBatchSize(5)
            .setMaxQueueSize(10)
            .build();
    LogEmitterProvider provider = LogEmitterProvider.builder().addLogProcessor(processor).build();
    LogEmitter emitter = provider.get("test", "0.1a");

    for (int i = 0; i < 7; i++) {
      emitter.emit(createLogRecord(Severity.WARN, "test #" + i));
    }
    // Ensure that more than batch size kicks off a flush
    await().atMost(Duration.ofSeconds(5)).until(() -> exporter.getRecords().size() > 0);
    // Ensure that everything gets through
    CompletableResultCode result = provider.forceFlush();
    result.join(1, TimeUnit.SECONDS);
    assertThat(exporter.getCallCount()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void testNoBlocking() {
    TestLogExporter exporter = new TestLogExporter();
    exporter.setOnCall(
        () -> {
          try {
            Thread.sleep(250);
          } catch (InterruptedException ex) {
            fail("Exporter wait interrupted", ex);
          }
        });
    LogProcessor processor =
        BatchLogProcessor.builder(exporter)
            .setScheduleDelayMillis(3000) // Long enough to not be in play
            .setMaxExportBatchSize(5)
            .setMaxQueueSize(10)
            .build();
    LogEmitterProvider provider = LogEmitterProvider.builder().addLogProcessor(processor).build();
    LogEmitter emitter = provider.get("test", "0.1a");

    long start = System.currentTimeMillis();
    int testRecordCount = 700;
    for (int i = 0; i < testRecordCount; i++) {
      emitter.emit(createLogRecord(Severity.WARN, "test #" + i));
    }
    long end = System.currentTimeMillis();
    assertThat(end - start).isLessThan(250L);
    provider.forceFlush().join(1, TimeUnit.SECONDS);
    assertThat(exporter.getRecords().size()).isLessThan(testRecordCount); // We dropped records
  }

  @Test
  void testMultipleProcessors() {
    TestLogProcessor processorOne = new TestLogProcessor();
    TestLogProcessor processorTwo = new TestLogProcessor();
    LogEmitterProvider provider =
        LogEmitterProvider.builder()
            .setResource(RESOURCE)
            .addLogProcessor(processorOne)
            .addLogProcessor(processorTwo)
            .build();
    LogEmitter emitter = provider.get(INSTRUMENTATION_LIBRARY, INSTRUMENTATION_LIBRARY_VERSION);
    LogRecord logRecord = createLogRecord(Severity.INFO, "test");
    emitter.emit(logRecord);
    LogData logData = toLogData(logRecord);
    assertThat(processorOne.getRecords().size()).isEqualTo(1);
    assertThat(processorTwo.getRecords().size()).isEqualTo(1);
    assertThat(processorOne.getRecords().get(0)).isEqualTo(logData);
    assertThat(processorTwo.getRecords().get(0)).isEqualTo(logData);

    CompletableResultCode flushResult = provider.forceFlush();
    flushResult.join(1, TimeUnit.SECONDS);
    assertThat(processorOne.getFlushes()).isEqualTo(1);
    assertThat(processorTwo.getFlushes()).isEqualTo(1);

    CompletableResultCode shutdownResult = provider.shutdown();
    shutdownResult.join(1, TimeUnit.SECONDS);
    assertThat(processorOne.shutdownHasBeenCalled()).isEqualTo(true);
    assertThat(processorTwo.shutdownHasBeenCalled()).isEqualTo(true);
  }
}
