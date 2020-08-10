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

import io.opentelemetry.logging.api.LogRecord;
import io.opentelemetry.logging.api.LogRecord.Severity;
import io.opentelemetry.logging.api.LogSink;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SdkLogSinkProviderTest {
  @Test
  public void testProviderAggregation() throws InterruptedException {
    List<LogRecord> records1 = new ArrayList<>();
    List<LogRecord> records2 = new ArrayList<>();
    SdkLogSinkProvider provider =
        new SdkLogSinkProvider.Builder()
            .withBatchManager(
                new SizeOrLatencyBatchStrategy.Builder()
                    .withMaxBatchSize(5)
                    .withMaxDelay(50, TimeUnit.MILLISECONDS)
                    .build())
            .withExporter(records1::addAll)
            .withExporter(records2::addAll)
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
    assertThat(records1.size()).isEqualTo(10);
    assertThat(records2.size()).isEqualTo(10);
    Thread.sleep(55);
    assertThat(records1.size()).isEqualTo(11);
    assertThat(records2.size()).isEqualTo(11);
    assertThat(records1).isEqualTo(records2);
  }
}
