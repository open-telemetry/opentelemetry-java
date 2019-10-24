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

package io.opentelemetry.exporters.logging;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import io.opentelemetry.sdk.common.Timestamp;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import org.junit.Test;

public class LoggingExporterTest {
  @Test
  public void returnCode() {
    LoggingExporter exporter = new LoggingExporter();
    long startTimeSecondsSinceEpoch = System.currentTimeMillis() / 1000;
    SpanData spanData =
        SpanData.newBuilder()
            .setTraceId(new TraceId(1234L, 6789L))
            .setSpanId(new SpanId(9876L))
            .setStartTimestamp(Timestamp.create(startTimeSecondsSinceEpoch, 0))
            .setEndTimestamp(Timestamp.create(startTimeSecondsSinceEpoch, 100))
            .setStatus(Status.OK)
            .setName("testSpan")
            .setKind(Kind.INTERNAL)
            .setTimedEvents(
                singletonList(
                    SpanData.TimedEvent.create(
                        Timestamp.create(startTimeSecondsSinceEpoch, 50),
                        "somethingHappenedHere",
                        singletonMap("important", AttributeValue.booleanAttributeValue(true)))))
            .build();
    ResultCode resultCode = exporter.export(singletonList(spanData));
    assertEquals(ResultCode.SUCCESS, resultCode);
  }
}
