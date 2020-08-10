package io.opentelemetry.logging;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.logging.api.LogRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
@RunWith(JUnit4.class)
public class SizeOrLatencyBatchStrategyTest {

  @Test
  public void testSizeStrategy() {
    SizeOrLatencyBatchStrategy strategy =
        new SizeOrLatencyBatchStrategy.Builder().withMaxBatchSize(5).build();
    final List<LogRecord> transmittedBatch = new ArrayList<>();

    strategy.setBatchHandler(transmittedBatch::addAll);

    for (int i = 0; i < 7; i++) {
      strategy.add(null);
    }

    assertThat(transmittedBatch.size()).isEqualTo(5);

    for (int i = 0; i < 3; i++) {
      strategy.add(null);
    }

    assertThat(transmittedBatch.size()).isEqualTo(10);
  }

  @Test
  public void testLatencyStrategy() throws InterruptedException {
    SizeOrLatencyBatchStrategy strategy =
        new SizeOrLatencyBatchStrategy.Builder().withMaxDelay(50, TimeUnit.MILLISECONDS).build();

    final List<LogRecord> transmittedBatch = new ArrayList<>();

    strategy.setBatchHandler(transmittedBatch::addAll);

    for (int i = 0; i < 7; i++) {
      strategy.add(null);
      Thread.sleep(10);
    }

    assertThat(transmittedBatch.size()).isEqualTo(5);
    Thread.sleep(55);
    assertThat(transmittedBatch.size()).isEqualTo(7);
  }
}
