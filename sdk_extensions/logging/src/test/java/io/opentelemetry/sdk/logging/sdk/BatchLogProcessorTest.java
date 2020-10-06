/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logging.sdk;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.logging.export.BatchLogProcessor;
import io.opentelemetry.sdk.logging.export.LogExporter;
import io.opentelemetry.sdk.logging.util.TestLogExporter;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class BatchLogProcessorTest {
  @Test
  void testBuilder() {
    Properties props = new Properties();
    long delay = 1234L;
    int queue = 2345;
    int batch = 521;
    int timeout = 5432;

    props.put("otel.log.schedule.delay", Long.toString(delay));
    props.put("otel.log.max.queue", Integer.toString(queue));
    props.put("otel.log.max.export.batch", Integer.toString(batch));
    props.put("otel.log.export.timeout", Integer.toString(timeout));

    BatchLogProcessor.Builder builder =
        BatchLogProcessor.builder(
            new LogExporter() {
              @Override
              public CompletableResultCode export(Collection<LogRecord> records) {
                return CompletableResultCode.ofSuccess();
              }

              @Override
              public CompletableResultCode shutdown() {
                return CompletableResultCode.ofSuccess();
              }
            });

    builder.readProperties(props);
    assertThat(builder.getScheduleDelayMillis()).isEqualTo(delay);
    assertThat(builder.getMaxQueueSize()).isEqualTo(queue);
    assertThat(builder.getMaxExportBatchSize()).isEqualTo(batch);
    assertThat(builder.getExporterTimeoutMillis()).isEqualTo(timeout);
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
      LogRecord record = LogRecord.builder().setBody(Integer.toString(i)).build();
      processor.addLogRecord(record);
    }
    await().until(() -> exporter.getCallCount() > 0);
    assertThat(exporter.getRecords().size()).isEqualTo(batchSize);
    processor.forceFlush().join(1, TimeUnit.SECONDS);
    assertThat(exporter.getRecords().size()).isEqualTo(testRecordsToSend);
  }
}
