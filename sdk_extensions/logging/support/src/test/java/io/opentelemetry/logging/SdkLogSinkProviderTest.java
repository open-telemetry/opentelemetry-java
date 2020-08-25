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

package io.opentelemetry.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.logging.api.Exporter;
import io.opentelemetry.logging.api.LogRecord;
import io.opentelemetry.logging.api.LogRecord.Severity;
import io.opentelemetry.logging.api.LogSink;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SdkLogSinkProviderTest {
  private static class TestExporter implements Exporter {
    private final List<LogRecord> records = new ArrayList<>();
    private int batches = 0;

    @Override
    public void accept(Collection<LogRecord> records) {
      this.records.addAll(records);
      batches++;
    }
  }

  @Test
  public void testProviderAggregation() {
    TestExporter exporter1 = new TestExporter();
    TestExporter exporter2 = new TestExporter();
    SdkLogSinkProvider provider =
        new SdkLogSinkProvider.Builder()
            .withBatchManager(
                new SizeOrLatencyBatchStrategy.Builder()
                    .withMaxBatchSize(5)
                    .withMaxDelay(250, TimeUnit.MILLISECONDS)
                    .build())
            .withExporter(exporter1)
            .withExporter(exporter2)
            .build();
    LogSink sink = provider.get("test", "0.8.0");
    for (int i = 0; i < 11; i++) {
      sink.offer(
          sink.buildRecord()
              .withUnixTimeMillis(System.currentTimeMillis())
              .withSeverity(Severity.DEBUG)
              .withBody("test")
              .build());
    }
    await()
        .atLeast(100, TimeUnit.MILLISECONDS)
        .atMost(500, TimeUnit.MILLISECONDS)
        .until(() -> exporter1.records.size() == 11);
    assertThat(exporter1.batches).isEqualTo(3);
  }
}
