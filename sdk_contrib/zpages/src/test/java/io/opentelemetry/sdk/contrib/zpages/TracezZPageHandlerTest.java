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

package io.opentelemetry.sdk.contrib.zpages;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracezZPageHandler}. */
@RunWith(JUnit4.class)
public final class TracezZPageHandlerTest {
  private final TestClock testClock = TestClock.create();
  private final TracerSdkProvider tracerSdkProvider =
      TracerSdkProvider.builder().setClock(testClock).build();
  private final Tracer tracer = tracerSdkProvider.get("TracezZPageHandlerTest");
  private final TracezSpanProcessor spanProcessor = TracezSpanProcessor.newBuilder().build();
  private final TracezDataAggregator dataAggregator = new TracezDataAggregator(spanProcessor);
  private static final String FINISHED_SPAN_ONE = "FinishedSpanOne";
  private static final String FINISHED_SPAN_TWO = "FinishedSpanTwo";
  private static final String RUNNING_SPAN_ONE = "RunningSpanOne";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    tracerSdkProvider.addSpanProcessor(spanProcessor);
  }

  @Test
  public void emitSummaryTableForEachSpan() {
    OutputStream output = new ByteArrayOutputStream();
    Span span1 = tracer.spanBuilder(FINISHED_SPAN_ONE).startSpan();
    Span span2 = tracer.spanBuilder(FINISHED_SPAN_TWO).startSpan();
    span1.end();
    span2.end();
    TracezZPageHandler tracezZPageHandler = TracezZPageHandler.create(dataAggregator);
    Map<String, String> queryMap = Collections.emptyMap();
    tracezZPageHandler.emitHtml(queryMap, output);
    assertThat(output.toString()).contains(FINISHED_SPAN_ONE);
    assertThat(output.toString()).contains(FINISHED_SPAN_TWO);
  }

  @Test
  public void linkForRunningSpansExistInSummaryTable() {
    OutputStream output = new ByteArrayOutputStream();
    Span span = tracer.spanBuilder(RUNNING_SPAN_ONE).startSpan();
    TracezZPageHandler tracezZPageHandler = TracezZPageHandler.create(dataAggregator);
    Map<String, String> queryMap = Collections.emptyMap();
    tracezZPageHandler.emitHtml(queryMap, output);
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + RUNNING_SPAN_ONE + "&ztype=0&zsubtype=0\"");
    span.end();
  }
}
