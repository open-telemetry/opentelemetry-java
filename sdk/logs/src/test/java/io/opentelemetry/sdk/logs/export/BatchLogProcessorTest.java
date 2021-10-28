/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static io.opentelemetry.sdk.logs.util.TestUtil.createLogData;
import static io.opentelemetry.sdk.logs.util.TestUtil.createLogRecord;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogEmitter;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.util.TestLogExporter;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class BatchLogProcessorTest {

  @Test
  void testBatchSize() {
    TestLogExporter exporter = new TestLogExporter();
    LogProcessor processor =
        BatchLogProcessor.builder(exporter)
            .setScheduleDelayMillis(10000) // Long enough to not be in play
            .setMaxExportBatchSize(5)
            .setMaxQueueSize(10)
            .build();
    SdkLogEmitterProvider provider =
        SdkLogEmitterProvider.builder().addLogProcessor(processor).build();
    LogEmitter emitter =
        provider.logEmitterBuilder("test").setInstrumentationVersion("0.1a").build();

    for (int i = 0; i < 7; i++) {
      emitter.emit(createLogRecord(Severity.WARN, "test #" + i));
    }
    // Ensure that more than batch size kicks off a flush
    await().atMost(Duration.ofSeconds(5)).until(() -> exporter.getRecords().size() > 0);
    // Ensure that everything gets through
    CompletableResultCode result = provider.forceFlush();
    result.join(1, TimeUnit.SECONDS);
    Assertions.assertThat(exporter.getCallCount()).isGreaterThanOrEqualTo(2);
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
    SdkLogEmitterProvider provider =
        SdkLogEmitterProvider.builder().addLogProcessor(processor).build();
    LogEmitter emitter =
        provider.logEmitterBuilder("test").setInstrumentationVersion("0.1a").build();

    long start = System.currentTimeMillis();
    int testRecordCount = 700;
    for (int i = 0; i < testRecordCount; i++) {
      emitter.emit(createLogRecord(Severity.WARN, "test #" + i));
    }
    long end = System.currentTimeMillis();
    Assertions.assertThat(end - start).isLessThan(250L);
    provider.forceFlush().join(1, TimeUnit.SECONDS);
    Assertions.assertThat(exporter.getRecords().size())
        .isLessThan(testRecordCount); // We dropped records
  }

  @Test
  void testForceExport() {
    int batchSize = 10;
    int testRecordsToSend = 17; // greater than, but not a multiple of batch
    TestLogExporter exporter = new TestLogExporter();
    BatchLogProcessor processor =
        BatchLogProcessor.builder(exporter)
            .setMaxExportBatchSize(batchSize)
            .setMaxQueueSize(20) // more than we will send
            .setScheduleDelayMillis(2000) // longer than test
            .build();
    for (int i = 0; i < 17; i++) {
      LogData logData = createLogData(Severity.INFO, Integer.toString(i));
      processor.emit(logData);
    }
    await().until(() -> exporter.getCallCount() > 0);
    assertThat(exporter.getRecords().size()).isEqualTo(batchSize);
    processor.forceFlush().join(1, TimeUnit.SECONDS);
    assertThat(exporter.getRecords().size()).isEqualTo(testRecordsToSend);
    processor.shutdown().join(1, TimeUnit.SECONDS);
  }
}
