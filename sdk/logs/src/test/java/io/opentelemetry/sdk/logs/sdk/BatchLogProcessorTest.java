/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.sdk;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.util.TestLogExporter;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class BatchLogProcessorTest {

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
      LogRecord record = LogRecord.builder().setBody(Integer.toString(i)).build();
      processor.addLogRecord(record);
    }
    await().until(() -> exporter.getCallCount() > 0);
    assertThat(exporter.getRecords().size()).isEqualTo(batchSize);
    processor.forceFlush().join(1, TimeUnit.SECONDS);
    assertThat(exporter.getRecords().size()).isEqualTo(testRecordsToSend);
    processor.shutdown().join(1, TimeUnit.SECONDS);
  }
}
