/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link MultiSpanExporterTest}. */
@ExtendWith(MockitoExtension.class)
class MultiSpanExporterTest {
  @Mock private SpanExporter spanExporter1;
  @Mock private SpanExporter spanExporter2;
  private static final List<SpanData> SPAN_LIST =
      Collections.singletonList(TestUtils.makeBasicSpan());

  @Test
  void empty() {
    SpanExporter multiSpanExporter = SpanExporter.composite(Collections.emptyList());
    multiSpanExporter.export(SPAN_LIST);
    multiSpanExporter.shutdown();
  }

  @Test
  void oneSpanExporter() {
    SpanExporter multiSpanExporter =
        SpanExporter.composite(Collections.singletonList(spanExporter1));

    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.export(SPAN_LIST).isSuccess()).isTrue();
    Mockito.verify(spanExporter1).export(same(SPAN_LIST));

    when(spanExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.flush().isSuccess()).isTrue();
    Mockito.verify(spanExporter1).flush();

    when(spanExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    multiSpanExporter.shutdown();
    Mockito.verify(spanExporter1).shutdown();
  }

  @Test
  void twoSpanExporter() {
    SpanExporter multiSpanExporter =
        SpanExporter.composite(Arrays.asList(spanExporter1, spanExporter2));

    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.export(SPAN_LIST).isSuccess()).isTrue();
    Mockito.verify(spanExporter1).export(same(SPAN_LIST));
    Mockito.verify(spanExporter2).export(same(SPAN_LIST));

    when(spanExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.flush().isSuccess()).isTrue();
    Mockito.verify(spanExporter1).flush();
    Mockito.verify(spanExporter2).flush();

    when(spanExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    multiSpanExporter.shutdown();
    Mockito.verify(spanExporter1).shutdown();
    Mockito.verify(spanExporter2).shutdown();
  }

  @Test
  void twoSpanExporter_OneReturnFailure() {
    SpanExporter multiSpanExporter =
        SpanExporter.composite(Arrays.asList(spanExporter1, spanExporter2));

    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiSpanExporter.export(SPAN_LIST).isSuccess()).isFalse();
    Mockito.verify(spanExporter1).export(same(SPAN_LIST));
    Mockito.verify(spanExporter2).export(same(SPAN_LIST));

    when(spanExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanExporter2.flush()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiSpanExporter.flush().isSuccess()).isFalse();
    Mockito.verify(spanExporter1).flush();
    Mockito.verify(spanExporter2).flush();

    when(spanExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanExporter2.shutdown()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiSpanExporter.shutdown().isSuccess()).isFalse();
    Mockito.verify(spanExporter1).shutdown();
    Mockito.verify(spanExporter2).shutdown();
  }

  @Test
  void twoSpanExporter_FirstThrows() {
    SpanExporter multiSpanExporter =
        SpanExporter.composite(Arrays.asList(spanExporter1, spanExporter2));

    Mockito.doThrow(new IllegalArgumentException("No export for you."))
        .when(spanExporter1)
        .export(ArgumentMatchers.anyList());
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.export(SPAN_LIST).isSuccess()).isFalse();
    Mockito.verify(spanExporter1).export(same(SPAN_LIST));
    Mockito.verify(spanExporter2).export(same(SPAN_LIST));

    Mockito.doThrow(new IllegalArgumentException("No flush for you.")).when(spanExporter1).flush();
    when(spanExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.flush().isSuccess()).isFalse();
    Mockito.verify(spanExporter1).flush();
    Mockito.verify(spanExporter2).flush();

    Mockito.doThrow(new IllegalArgumentException("No shutdown for you."))
        .when(spanExporter1)
        .shutdown();
    when(spanExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.shutdown().isSuccess()).isFalse();
    Mockito.verify(spanExporter1).shutdown();
    Mockito.verify(spanExporter2).shutdown();
  }
}
