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

import com.google.common.primitives.Longs;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.spans.SpanBatch;
import io.opentelemetry.proto.trace.v1.AttributeValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanBatchAdapterTest {

  @Test
  void testSendBatchWithSingleSpan() {
    byte[] spanIdBytes = Longs.toByteArray(1234565L);
    byte[] bigTraceId = new byte[16];
    System.arraycopy(Longs.toByteArray(6543215L), 0, bigTraceId, 0, 8);
    System.arraycopy(Longs.toByteArray(939393939L), 0, bigTraceId, 8, 8);
    byte[] parentSpanIdBytes = Longs.toByteArray(777666777L);

    com.newrelic.telemetry.spans.Span span1 =
        com.newrelic.telemetry.spans.Span.builder("000000000012d685")
            .traceId("000000000063d76f0000000037fe0393")
            .timestamp(1000456)
            .name("spanName")
            .parentId("000000002e5a40d9")
            .durationMs(1333.020111d)
            .build();
    SpanBatch expected = new SpanBatch(Collections.singleton(span1), new Attributes());

    SpanBatchAdapter testClass = new SpanBatchAdapter(new Attributes());

    Span inputSpan =
        Span.newBuilder()
            .setSpanId(ByteString.copyFrom(spanIdBytes))
            .setTraceId(ByteString.copyFrom(bigTraceId))
            .setStartTime(Timestamp.newBuilder().setSeconds(1000).setNanos(456_000_000).build())
            .setParentSpanId(ByteString.copyFrom(parentSpanIdBytes))
            .setEndTime(Timestamp.newBuilder().setSeconds(1001).setNanos(789_020_111).build())
            .setName("spanName")
            .setStatus(Status.newBuilder().setCode(200).build())
            .build();

    SpanBatch result = testClass.adaptToSpanBatch(Collections.singletonList(inputSpan));
    assertEquals(expected, result);
  }

  @Test
  void testAttributes() {
    byte[] spanIdBytes = Longs.toByteArray(1234565L);
    com.newrelic.telemetry.spans.Span span1 =
        com.newrelic.telemetry.spans.Span.builder("000000000012d685")
            .timestamp(1000456)
            .attributes(
                new Attributes()
                    .put("myBooleanKey", true)
                    .put("myIntKey", 123L)
                    .put("myStringKey", "attrValue")
                    .put("myDoubleKey", 123.45d))
            .build();

    SpanBatch expected = new SpanBatch(Collections.singleton(span1), new Attributes());

    SpanBatchAdapter testClass = new SpanBatchAdapter(new Attributes());

    Span inputSpan =
        Span.newBuilder()
            .setSpanId(ByteString.copyFrom(spanIdBytes))
            .setStartTime(Timestamp.newBuilder().setSeconds(1000).setNanos(456_000_000).build())
            .setAttributes(
                Span.Attributes.newBuilder()
                    .putAttributeMap(
                        "myBooleanKey", AttributeValue.newBuilder().setBoolValue(true).build())
                    .putAttributeMap(
                        "myIntKey", AttributeValue.newBuilder().setIntValue(123).build())
                    .putAttributeMap(
                        "myStringKey",
                        AttributeValue.newBuilder().setStringValue("attrValue").build())
                    .putAttributeMap(
                        "myDoubleKey", AttributeValue.newBuilder().setDoubleValue(123.45d).build()))
            .build();

    SpanBatch result = testClass.adaptToSpanBatch(Collections.singletonList(inputSpan));
    assertEquals(expected, result);
  }

  @Test
  void testMinimalData() {
    byte[] spanIdBytes = Longs.toByteArray(1234565L);
    SpanBatchAdapter testClass = new SpanBatchAdapter(new Attributes());

    Span inputSpan = Span.newBuilder().setSpanId(ByteString.copyFrom(spanIdBytes)).build();
    SpanBatch result = testClass.adaptToSpanBatch(Collections.singletonList(inputSpan));

    assertEquals(1, result.getTelemetry().size());
  }

  @Test
  void testErrors() {
    byte[] spanIdBytes = Longs.toByteArray(1234565L);
    com.newrelic.telemetry.spans.Span span1 =
        com.newrelic.telemetry.spans.Span.builder("000000000012d685")
            .timestamp(1000456)
            .attributes(new Attributes().put("error.message", "it's broken"))
            .build();
    SpanBatch expected =
        new SpanBatch(Collections.singleton(span1), new Attributes().put("host", "localhost"));

    SpanBatchAdapter testClass = new SpanBatchAdapter(new Attributes().put("host", "localhost"));

    Span inputSpan =
        Span.newBuilder()
            .setSpanId(ByteString.copyFrom(spanIdBytes))
            .setStartTime(Timestamp.newBuilder().setSeconds(1000).setNanos(456_000_000).build())
            .setStatus(Status.newBuilder().setMessage("it's broken"))
            .build();
    SpanBatch result = testClass.adaptToSpanBatch(Collections.singletonList(inputSpan));

    assertEquals(expected, result);
  }
}
