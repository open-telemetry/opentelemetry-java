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

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;

import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link MultiSpanExporterTest}. */
class MultiSpanExporterTest {
  @Mock private SpanExporter spanExporter1;
  @Mock private SpanExporter spanExporter2;
  private static final List<SpanData> SPAN_LIST =
      Collections.singletonList(TestUtils.makeBasicSpan());

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void empty() {
    SpanExporter multiSpanExporter = MultiSpanExporter.create(Collections.emptyList());
    multiSpanExporter.export(SPAN_LIST);
    multiSpanExporter.shutdown();
  }

  @Test
  void oneSpanExporter() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Collections.singletonList(spanExporter1));

    Mockito.when(spanExporter1.export(same(SPAN_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.export(SPAN_LIST).isSuccess()).isTrue();
    Mockito.verify(spanExporter1).export(same(SPAN_LIST));

    Mockito.when(spanExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.flush().isSuccess()).isTrue();
    Mockito.verify(spanExporter1).flush();

    multiSpanExporter.shutdown();
    Mockito.verify(spanExporter1).shutdown();
  }

  @Test
  void twoSpanExporter() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));

    Mockito.when(spanExporter1.export(same(SPAN_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    Mockito.when(spanExporter2.export(same(SPAN_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.export(SPAN_LIST).isSuccess()).isTrue();
    Mockito.verify(spanExporter1).export(same(SPAN_LIST));
    Mockito.verify(spanExporter2).export(same(SPAN_LIST));

    Mockito.when(spanExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    Mockito.when(spanExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.flush().isSuccess()).isTrue();
    Mockito.verify(spanExporter1).flush();
    Mockito.verify(spanExporter2).flush();

    multiSpanExporter.shutdown();
    Mockito.verify(spanExporter1).shutdown();
    Mockito.verify(spanExporter2).shutdown();
  }

  @Test
  void twoSpanExporter_OneReturnFailure() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));

    Mockito.when(spanExporter1.export(same(SPAN_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    Mockito.when(spanExporter2.export(same(SPAN_LIST)))
        .thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiSpanExporter.export(SPAN_LIST).isSuccess()).isFalse();
    Mockito.verify(spanExporter1).export(same(SPAN_LIST));
    Mockito.verify(spanExporter2).export(same(SPAN_LIST));

    Mockito.when(spanExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    Mockito.when(spanExporter2.flush()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiSpanExporter.flush().isSuccess()).isFalse();
    Mockito.verify(spanExporter1).flush();
    Mockito.verify(spanExporter2).flush();
  }

  @Test
  void twoSpanExporter_FirstThrows() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));

    Mockito.doThrow(new IllegalArgumentException("No export for you."))
        .when(spanExporter1)
        .export(ArgumentMatchers.anyList());
    Mockito.when(spanExporter2.export(same(SPAN_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.export(SPAN_LIST).isSuccess()).isFalse();
    Mockito.verify(spanExporter1).export(same(SPAN_LIST));
    Mockito.verify(spanExporter2).export(same(SPAN_LIST));

    Mockito.doThrow(new IllegalArgumentException("No flush for you.")).when(spanExporter1).flush();
    Mockito.when(spanExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiSpanExporter.flush().isSuccess()).isFalse();
    Mockito.verify(spanExporter1).flush();
    Mockito.verify(spanExporter2).flush();
  }
}
