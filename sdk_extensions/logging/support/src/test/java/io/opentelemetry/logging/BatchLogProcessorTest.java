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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import io.opentelemetry.logging.api.LogRecord;
import io.opentelemetry.logging.api.export.LogExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Collection;
import java.util.Properties;
import org.junit.Test;

public class BatchLogProcessorTest {

  @Test
  public void testBuilder() {
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
}
