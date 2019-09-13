/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.exporters.newrelic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewRelicSpanExporterTest {

  @Mock private SpanBatchSender spanBatchSender;

  @Test
  void testSpanSending() throws Exception {
    com.newrelic.telemetry.spans.Span span1 =
        com.newrelic.telemetry.spans.Span.builder("spanIdNumber1").timestamp(1000000).build();
    SpanBatch expected = new SpanBatch(Collections.singleton(span1), new Attributes());

    NewRelicSpanExporter testClass = new NewRelicSpanExporter(spanBatchSender, new Attributes());

    Span inputSpan =
        Span.newBuilder()
            .setSpanId(ByteString.copyFromUtf8("spanIdNumber1"))
            .setStartTime(Timestamp.newBuilder().setSeconds(1000).build())
            .build();
    ResultCode result = testClass.export(Collections.singletonList(inputSpan));

    assertEquals(ResultCode.SUCCESS, result);
    verify(spanBatchSender).sendBatch(expected);
  }
}
