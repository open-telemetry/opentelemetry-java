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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link MultiSpanExporterTest}. */
@RunWith(JUnit4.class)
public class MultiSpanExporterTest {
  @Mock private SpanExporter spanExporter1;
  @Mock private SpanExporter spanExporter2;
  private static final List<SpanData> SPAN_LIST =
      Collections.singletonList(TestUtils.makeBasicSpan());

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void empty() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Collections.<SpanExporter>emptyList());
    multiSpanExporter.export(SPAN_LIST);
    multiSpanExporter.shutdown();
  }

  @Test
  public void oneSpanExporter() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Collections.singletonList(spanExporter1));
    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.SUCCESS);
    verify(spanExporter1).export(same(SPAN_LIST));

    multiSpanExporter.shutdown();
    verify(spanExporter1).shutdown();
  }

  @Test
  public void twoSpanExporter() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));
    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.SUCCESS);
    verify(spanExporter1).export(same(SPAN_LIST));
    verify(spanExporter2).export(same(SPAN_LIST));

    multiSpanExporter.shutdown();
    verify(spanExporter1).shutdown();
    verify(spanExporter2).shutdown();
  }

  @Test
  public void twoSpanExporter_OneReturnNoneRetryable() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));
    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(ResultCode.FAILED_NOT_RETRYABLE);
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.FAILED_NOT_RETRYABLE);
    verify(spanExporter1).export(same(SPAN_LIST));
    verify(spanExporter2).export(same(SPAN_LIST));
  }

  @Test
  public void twoSpanExporter_OneReturnRetryable() {
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));
    when(spanExporter1.export(same(SPAN_LIST))).thenReturn(ResultCode.SUCCESS);
    when(spanExporter2.export(same(SPAN_LIST))).thenReturn(ResultCode.FAILED_RETRYABLE);
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.FAILED_RETRYABLE);
    verify(spanExporter1).export(same(SPAN_LIST));
    verify(spanExporter2).export(same(SPAN_LIST));
  }

  @Test
  public void twoSpanExporter_FirstThrows() {
    doThrow(new IllegalArgumentException("No export for you."))
        .when(spanExporter1)
        .export(ArgumentMatchers.<SpanData>anyList());
    SpanExporter multiSpanExporter =
        MultiSpanExporter.create(Arrays.asList(spanExporter1, spanExporter2));
    assertThat(multiSpanExporter.export(SPAN_LIST)).isEqualTo(ResultCode.FAILED_NOT_RETRYABLE);
    verify(spanExporter1).export(same(SPAN_LIST));
    verify(spanExporter2).export(same(SPAN_LIST));
  }
}
