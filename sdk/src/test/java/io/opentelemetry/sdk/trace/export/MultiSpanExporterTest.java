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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
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

    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.SUCCESS);
    verify(spanExporter1).export(same(SPAN_LIST));

    when(spanExporter1.flush()).thenReturn(ResultCode.SUCCESS);
    assertThat(multiSpanExporter.flush()).isEqualTo(ResultCode.SUCCESS);
    verify(spanExporter1).flush();

    multiSpanExporter.shutdown();
    verify(spanExporter1).shutdown();
  }

  @Test
  void twoSpanExporter() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));

    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.SUCCESS);
    verify(spanExporter1).export(same(SPAN_LIST));
    verify(spanExporter2).export(same(SPAN_LIST));

    when(spanExporter1.flush()).thenReturn(ResultCode.SUCCESS);
    when(spanExporter2.flush()).thenReturn(ResultCode.SUCCESS);
    assertThat(multiSpanExporter.flush()).isEqualTo(ResultCode.SUCCESS);
    verify(spanExporter1).flush();
    verify(spanExporter2).flush();

    multiSpanExporter.shutdown();
    verify(spanExporter1).shutdown();
    verify(spanExporter2).shutdown();
  }

  @Test
  void twoSpanExporter_OneReturnFailure() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));

    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(ResultCode.FAILURE);
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.FAILURE);
    verify(spanExporter1).export(same(SPAN_LIST));
    verify(spanExporter2).export(same(SPAN_LIST));

    when(spanExporter1.flush()).thenReturn(ResultCode.SUCCESS);
    when(spanExporter2.flush()).thenReturn(ResultCode.FAILURE);
    assertThat(multiSpanExporter.flush()).isEqualTo(ResultCode.FAILURE);
    verify(spanExporter1).flush();
    verify(spanExporter2).flush();
  }

  @Test
  void twoSpanExporter_FirstThrows() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));

    doThrow(new IllegalArgumentException("No export for you."))
        .when(spanExporter1)
        .export(ArgumentMatchers.anyList());
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.FAILURE);
    verify(spanExporter1).export(same(SPAN_LIST));
    verify(spanExporter2).export(same(SPAN_LIST));

    doThrow(new IllegalArgumentException("No flush for you.")).when(spanExporter1).flush();
    when(spanExporter2.flush()).thenReturn(ResultCode.SUCCESS);
    assertThat(multiSpanExporter.flush()).isEqualTo(ResultCode.FAILURE);
    verify(spanExporter1).flush();
    verify(spanExporter2).flush();
  }
}
