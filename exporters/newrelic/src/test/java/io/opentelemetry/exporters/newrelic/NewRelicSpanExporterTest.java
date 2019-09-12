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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import com.google.common.primitives.Longs;
import com.google.protobuf.ByteString;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.exceptions.DiscardBatchException;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewRelicSpanExporterTest {

  @Mock private SpanBatchSender sender;
  @Mock private SpanBatchAdapter adapter;

  @Test
  void testExportHappyPath() {
    NewRelicSpanExporter testClass = new NewRelicSpanExporter(adapter, sender);

    Span inputSpan =
        Span.newBuilder().setSpanId(ByteString.copyFrom(Longs.toByteArray(1234565L))).build();
    List<Span> spans = Collections.singletonList(inputSpan);
    SpanBatch batch =
        new SpanBatch(Collections.<com.newrelic.telemetry.spans.Span>emptyList(), new Attributes());
    when(adapter.adaptToSpanBatch(spans)).thenReturn(batch);

    ResultCode result = testClass.export(spans);
    assertEquals(ResultCode.SUCCESS, result);
  }

  @Test
  void testDiscardBatchException() throws Exception {
    checkResponseCodeProducesException(
        ResultCode.FAILED_NOT_RETRYABLE, DiscardBatchException.class);
  }

  @Test
  void testRetryWithSplitException() throws Exception {
    checkResponseCodeProducesException(
        ResultCode.FAILED_NOT_RETRYABLE, RetryWithSplitException.class);
  }

  @Test
  void testRetryWithBackOffException() throws Exception {
    checkResponseCodeProducesException(
        ResultCode.FAILED_RETRYABLE, RetryWithBackoffException.class);
  }

  @Test
  void testRetryWithRequestedWaitException() throws Exception {
    checkResponseCodeProducesException(
        ResultCode.FAILED_RETRYABLE, RetryWithRequestedWaitException.class);
  }

  private void checkResponseCodeProducesException(
      ResultCode resultCode, Class<? extends ResponseException> exceptionClass)
      throws ResponseException {
    com.newrelic.telemetry.spans.Span span =
        com.newrelic.telemetry.spans.Span.builder("000000000012d685").build();
    SpanBatch spanBatch = new SpanBatch(Collections.singleton(span), new Attributes());

    NewRelicSpanExporter testClass = new NewRelicSpanExporter(adapter, sender);

    Span inputSpan =
        Span.newBuilder().setSpanId(ByteString.copyFrom(Longs.toByteArray(1234565L))).build();

    when(adapter.adaptToSpanBatch(ArgumentMatchers.<Span>anyList())).thenReturn(spanBatch);
    when(sender.sendBatch(isA(SpanBatch.class))).thenThrow(exceptionClass);

    ResultCode result = testClass.export(Collections.singletonList(inputSpan));
    assertEquals(resultCode, result);
  }
}
