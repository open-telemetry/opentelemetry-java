/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.logging.data.LogRecord.Severity;
import io.opentelemetry.sdk.logging.export.BatchLogProcessor;
import io.opentelemetry.sdk.logging.export.LogExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LogSinkSdkProviderTest {
  private static class TestLogExporter implements LogExporter {
    private final ArrayList<LogRecord> records = new ArrayList<>();
    @Nullable private Runnable onCall = null;
    private int callCount = 0;

    @Override
    public CompletableResultCode export(Collection<LogRecord> records) {
      this.records.addAll(records);
      callCount++;
      if (onCall != null) {
        onCall.run();
      }
      return null;
    }

    @Override
    public CompletableResultCode shutdown() {
      return new CompletableResultCode().succeed();
    }
  }

  private static LogRecord createLog(LogRecord.Severity severity, String message) {
    return new LogRecord.Builder()
        .withUnixTimeMillis(System.currentTimeMillis())
        .withSeverity(severity)
        .withBody(message)
        .build();
  }

  @Test
  public void testLogSinkSdkProvider() {
    TestLogExporter exporter = new TestLogExporter();
    LogProcessor processor = BatchLogProcessor.builder(exporter).build();
    LogSinkSdkProvider provider = new LogSinkSdkProvider.Builder().build();
    provider.addLogProcessor(processor);
    LogSink sink = provider.get("test", "0.1a");
    sink.offer(createLog(Severity.ERROR, "test"));
    provider.forceFlush();
    await().atMost(500, TimeUnit.MILLISECONDS).until(() -> exporter.records.size() > 0);
    assertThat(exporter.records.size()).isEqualTo(1);
  }

  @Test
  public void testBatchSize() {
    TestLogExporter exporter = new TestLogExporter();
    LogProcessor processor =
        BatchLogProcessor.builder(exporter)
            .setScheduleDelayMillis(3000) // Long enough to not be in play
            .setMaxExportBatchSize(5)
            .setMaxQueueSize(10)
            .build();
    LogSinkSdkProvider provider = new LogSinkSdkProvider.Builder().build();
    provider.addLogProcessor(processor);
    LogSink sink = provider.get("test", "0.1a");

    for (int i = 0; i < 7; i++) {
      sink.offer(createLog(Severity.WARN, "test #" + i));
    }
    // Ensure that more than batch size kicks off a flush
    await().atMost(500, TimeUnit.MILLISECONDS).until(() -> exporter.records.size() > 0);
    // Ensure that everything gets through
    provider.forceFlush();
    await().atMost(500, TimeUnit.MILLISECONDS).until(() -> exporter.records.size() == 7);
    assertThat(exporter.callCount).isGreaterThanOrEqualTo(2);
  }

  @Test
  public void testNoBlocking() {
    TestLogExporter exporter = new TestLogExporter();
    exporter.onCall =
        () -> {
          try {
            Thread.sleep(250);
          } catch (InterruptedException ex) {
            fail("Exporter wait interrupted", ex);
          }
        };
    LogProcessor processor =
        BatchLogProcessor.builder(exporter)
            .setScheduleDelayMillis(3000) // Long enough to not be in play
            .setMaxExportBatchSize(5)
            .setMaxQueueSize(10)
            .build();
    LogSinkSdkProvider provider = new LogSinkSdkProvider.Builder().build();
    provider.addLogProcessor(processor);
    LogSink sink = provider.get("test", "0.1a");

    long start = System.currentTimeMillis();
    for (int i = 0; i < 700; i++) {
      sink.offer(createLog(Severity.WARN, "test #" + i));
    }
    long end = System.currentTimeMillis();
    assertThat(end - start).isLessThan(250L);
    await().atMost(1000, TimeUnit.MILLISECONDS).until(() -> exporter.callCount == 2);
    assertThat(exporter.records.size()) // Two exporter batches
        .isGreaterThan(5)
        .isLessThanOrEqualTo(10);
  }
}
