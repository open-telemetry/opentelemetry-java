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

import io.opentelemetry.logging.api.LogRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SizeOrLatencyBatchStrategyTest {
  private static class TestBatchHandler implements LoggingBatchExporter {
    private final List<LogRecord> records = new ArrayList<>();
    private int callCount = 0;

    @Override
    public void handleLogRecordBatch(Collection<LogRecord> batch) {
      records.addAll(batch);
      callCount++;
    }
  }

  @Test
  public void testSizeStrategy() {
    SizeOrLatencyBatchStrategy strategy =
        new SizeOrLatencyBatchStrategy.Builder()
            .withMaxBatchSize(5)
            .withMaxDelay(1, TimeUnit.DAYS)
            .build();
    final List<LogRecord> transmittedBatch = new ArrayList<>();

    strategy.setBatchHandler(c -> transmittedBatch.addAll(c));

    for (int i = 0; i < 7; i++) {
      LogRecord record = new LogRecord.Builder().build();
      strategy.add(record);
    }

    assertThat(transmittedBatch.size()).isEqualTo(5);

    for (int i = 0; i < 3; i++) {
      LogRecord record = new LogRecord.Builder().build();
      strategy.add(record);
    }

    assertThat(transmittedBatch.size()).isEqualTo(10);
  }

  @Test
  public void testLatencyStrategy() throws InterruptedException {
    int maxDelay = 50;
    TestBatchHandler batchHandler = new TestBatchHandler();
    SizeOrLatencyBatchStrategy strategy =
        new SizeOrLatencyBatchStrategy.Builder()
            .withMaxDelay(maxDelay, TimeUnit.MILLISECONDS)
            .build();

    strategy.setBatchHandler(batchHandler);

    for (int i = 0; i < 7; i++) {
      LogRecord record = new LogRecord.Builder().build();
      strategy.add(record);
      Thread.sleep(10);
    }

    await().atMost(200, TimeUnit.MILLISECONDS).until(() -> batchHandler.records.size() == 7);

    assertThat(batchHandler.callCount).isEqualTo(2);
  }
}
