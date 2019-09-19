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
import static org.mockito.Mockito.when;

import com.google.common.primitives.Longs;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.exceptions.DiscardBatchException;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import io.opentelemetry.proto.trace.v1.AttributeValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewRelicSpanExporterTest {

  @Mock private SpanBatchSender spanBatchSender;

  @Test
  void testSendBatchWithSingleSpan() throws Exception {
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

    NewRelicSpanExporter testClass = new NewRelicSpanExporter(spanBatchSender, new Attributes());

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
    ResultCode result = testClass.export(Collections.singletonList(inputSpan));

    assertEquals(ResultCode.SUCCESS, result);
    verify(spanBatchSender).sendBatch(expected);
  }

  @Test
  void testAttributes() throws Exception {
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

    NewRelicSpanExporter testClass = new NewRelicSpanExporter(spanBatchSender, new Attributes());

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
    ResultCode result = testClass.export(Collections.singletonList(inputSpan));

    assertEquals(ResultCode.SUCCESS, result);
    verify(spanBatchSender).sendBatch(expected);
  }

  @Test
  void testMinimalData() throws Exception {
    byte[] spanIdBytes = Longs.toByteArray(1234565L);
    NewRelicSpanExporter testClass = new NewRelicSpanExporter(spanBatchSender, new Attributes());

    Span inputSpan = Span.newBuilder().setSpanId(ByteString.copyFrom(spanIdBytes)).build();
    ResultCode result = testClass.export(Collections.singletonList(inputSpan));

    assertEquals(ResultCode.SUCCESS, result);
    ArgumentCaptor<SpanBatch> spanBatchCapture = ArgumentCaptor.forClass(SpanBatch.class);
    verify(spanBatchSender).sendBatch(spanBatchCapture.capture());
    SpanBatch observedBatch = spanBatchCapture.getAllValues().get(0);
    Collection<com.newrelic.telemetry.spans.Span> telemetry = observedBatch.getTelemetry();
    assertEquals(1, telemetry.size());
  }

  @Test
  void testErrors() throws Exception {
    byte[] spanIdBytes = Longs.toByteArray(1234565L);
    com.newrelic.telemetry.spans.Span span1 =
        com.newrelic.telemetry.spans.Span.builder("000000000012d685")
            .timestamp(1000456)
            .attributes(new Attributes().put("error.message", "it's broken"))
            .build();
    SpanBatch expected =
        new SpanBatch(Collections.singleton(span1), new Attributes().put("host", "localhost"));

    NewRelicSpanExporter testClass =
        new NewRelicSpanExporter(spanBatchSender, new Attributes().put("host", "localhost"));

    Span inputSpan =
        Span.newBuilder()
            .setSpanId(ByteString.copyFrom(spanIdBytes))
            .setStartTime(Timestamp.newBuilder().setSeconds(1000).setNanos(456_000_000).build())
            .setStatus(Status.newBuilder().setMessage("it's broken"))
            .build();
    ResultCode result = testClass.export(Collections.singletonList(inputSpan));

    assertEquals(ResultCode.SUCCESS, result);
    verify(spanBatchSender).sendBatch(expected);
  }

  private static Stream<Arguments> codesAndExpectedExceptions() {
    return Stream.of(
        Arguments.of(ResultCode.FAILED_RETRYABLE, RetryWithRequestedWaitException.class),
        Arguments.of(ResultCode.FAILED_RETRYABLE, RetryWithBackoffException.class),
        Arguments.of(ResultCode.FAILED_NOT_RETRYABLE, RetryWithSplitException.class),
        Arguments.of(ResultCode.FAILED_NOT_RETRYABLE, DiscardBatchException.class));
  }

  @ParameterizedTest
  @MethodSource("codesAndExpectedExceptions")
  @DisplayName("Exporter responds with appropriate response code to exceptions")
  void testExporterResponseCodes(
      ResultCode resultCode, Class<? extends ResponseException> exceptionClass) throws Exception {
    byte[] spanIdBytes = Longs.toByteArray(1234565L);
    com.newrelic.telemetry.spans.Span span1 =
        com.newrelic.telemetry.spans.Span.builder("000000000012d685")
            .timestamp(1000456)
            .attributes(new Attributes().put("error.message", "Some Exception"))
            .build();
    SpanBatch expected =
        new SpanBatch(Collections.singleton(span1), new Attributes().put("host", "localhost"));

    NewRelicSpanExporter testClass =
        new NewRelicSpanExporter(spanBatchSender, new Attributes().put("host", "localhost"));

    Span inputSpan =
        Span.newBuilder()
            .setSpanId(ByteString.copyFrom(spanIdBytes))
            .setStartTime(Timestamp.newBuilder().setSeconds(1000).setNanos(456_000_000).build())
            .setStatus(Status.newBuilder().setMessage("Some Exception"))
            .build();

    when(spanBatchSender.sendBatch(expected)).thenThrow(exceptionClass);
    ResultCode result = testClass.export(Collections.singletonList(inputSpan));

    assertEquals(resultCode, result);
  }
}
