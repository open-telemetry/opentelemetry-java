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

package io.opentelemetry.sdk.extensions.zpages;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.EndSpanOptions;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status.CanonicalCode;
import io.opentelemetry.trace.Tracer;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/** Unit tests for {@link TracezZPageHandler}. */
@RunWith(JUnit4.class)
public final class TracezZPageHandlerTest {
  private static final String FINISHED_SPAN_ONE = "FinishedSpanOne";
  private static final String FINISHED_SPAN_TWO = "FinishedSpanTwo";
  private static final String RUNNING_SPAN = "RunningSpan";
  private static final String LATENCY_SPAN = "LatencySpan";
  private static final String ERROR_SPAN = "ErrorSpan";
  private final TestClock testClock = TestClock.create();
  private final TracerSdkProvider tracerSdkProvider =
      TracerSdkProvider.builder().setClock(testClock).build();
  private final Tracer tracer = tracerSdkProvider.get("TracezZPageHandlerTest");
  private final TracezSpanProcessor spanProcessor = TracezSpanProcessor.newBuilder().build();
  private final TracezDataAggregator dataAggregator = new TracezDataAggregator(spanProcessor);
  private final Map<String, String> emptyQueryMap = ImmutableMap.of();

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void setup() {
    tracerSdkProvider.addSpanProcessor(spanProcessor);
  }

  @Test
  public void summaryTable_emitRowForEachSpan() {
    OutputStream output = new ByteArrayOutputStream();
    Span finishedSpan1 = tracer.spanBuilder(FINISHED_SPAN_ONE).startSpan();
    Span finishedSpan2 = tracer.spanBuilder(FINISHED_SPAN_TWO).startSpan();
    finishedSpan1.end();
    finishedSpan2.end();

    Span runningSpan = tracer.spanBuilder(RUNNING_SPAN).startSpan();

    Span latencySpan = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions = EndSpanOptions.builder().setEndTimestamp(10002L).build();
    latencySpan.end(endOptions);

    Span errorSpan = tracer.spanBuilder(ERROR_SPAN).startSpan();
    errorSpan.setStatus(CanonicalCode.INVALID_ARGUMENT.toStatus());
    errorSpan.end();

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(emptyQueryMap, output);

    // Emit a row for all types of spans
    assertThat(output.toString()).contains(FINISHED_SPAN_ONE);
    assertThat(output.toString()).contains(FINISHED_SPAN_TWO);
    assertThat(output.toString()).contains(RUNNING_SPAN);
    assertThat(output.toString()).contains(LATENCY_SPAN);
    assertThat(output.toString()).contains(ERROR_SPAN);

    runningSpan.end();
  }

  @Test
  public void summaryTable_linkForRunningSpans() {
    OutputStream output = new ByteArrayOutputStream();
    Span runningSpan1 = tracer.spanBuilder(RUNNING_SPAN).startSpan();
    Span runningSpan2 = tracer.spanBuilder(RUNNING_SPAN).startSpan();
    Span runningSpan3 = tracer.spanBuilder(RUNNING_SPAN).startSpan();
    Span finishedSpan = tracer.spanBuilder(FINISHED_SPAN_ONE).startSpan();
    finishedSpan.end();

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(emptyQueryMap, output);

    // Link for running span with 3 running
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + RUNNING_SPAN + "&ztype=0&zsubtype=0\">3");
    // No link for finished spans
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + FINISHED_SPAN_ONE + "&ztype=0&subtype=0\"");

    runningSpan1.end();
    runningSpan2.end();
    runningSpan3.end();
  }

  @Test
  public void summaryTable_linkForLatencyBasedSpans_NoneForEmptyBoundary() {
    OutputStream output = new ByteArrayOutputStream();
    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(emptyQueryMap, output);

    // No link for boundary 0
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=0\"");
    // No link for boundary 1
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=1\"");
    // No link for boundary 2
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=2\"");
    // No link for boundary 3
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=3\"");
    // No link for boundary 4
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=4\"");
    // No link for boundary 5
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=5\"");
    // No link for boundary 6
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=6\"");
    // No link for boundary 7
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=7\"");
    // No link for boundary 8
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=8\"");
  }

  @Test
  public void summaryTable_linkForLatencyBasedSpans_OnePerBoundary() {
    OutputStream output = new ByteArrayOutputStream();
    // Boundary 0, >1us
    Span latencySpanSubtype0 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions0 = EndSpanOptions.builder().setEndTimestamp(1002L).build();
    latencySpanSubtype0.end(endOptions0);
    // Boundary 1, >10us
    Span latencySpanSubtype1 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions1 = EndSpanOptions.builder().setEndTimestamp(10002L).build();
    latencySpanSubtype1.end(endOptions1);
    // Boundary 2, >100us
    Span latencySpanSubtype2 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions2 = EndSpanOptions.builder().setEndTimestamp(100002L).build();
    latencySpanSubtype2.end(endOptions2);
    // Boundary 3, >1ms
    Span latencySpanSubtype3 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions3 = EndSpanOptions.builder().setEndTimestamp(1000002L).build();
    latencySpanSubtype3.end(endOptions3);
    // Boundary 4, >10ms
    Span latencySpanSubtype4 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions4 = EndSpanOptions.builder().setEndTimestamp(10000002L).build();
    latencySpanSubtype4.end(endOptions4);
    // Boundary 5, >100ms
    Span latencySpanSubtype5 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions5 = EndSpanOptions.builder().setEndTimestamp(100000002L).build();
    latencySpanSubtype5.end(endOptions5);
    // Boundary 6, >1s
    Span latencySpanSubtype6 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions6 = EndSpanOptions.builder().setEndTimestamp(1000000002L).build();
    latencySpanSubtype6.end(endOptions6);
    // Boundary 7, >10s
    Span latencySpanSubtype7 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions7 = EndSpanOptions.builder().setEndTimestamp(10000000002L).build();
    latencySpanSubtype7.end(endOptions7);
    // Boundary 8, >100s
    Span latencySpanSubtype8 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions8 = EndSpanOptions.builder().setEndTimestamp(100000000002L).build();
    latencySpanSubtype8.end(endOptions8);

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(emptyQueryMap, output);

    // Link for boundary 0
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=0\">1");
    // Link for boundary 1
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=1\">1");
    // Link for boundary 2
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=2\">1");
    // Link for boundary 3
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=3\">1");
    // Link for boundary 4
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=4\">1");
    // Link for boundary 5
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=5\">1");
    // Link for boundary 6
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=6\">1");
    // Link for boundary 7
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=7\">1");
    // Link for boundary 8
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=8\">1");
  }

  @Test
  public void summaryTable_linkForLatencyBasedSpans_MultipleForOneBoundary() {
    OutputStream output = new ByteArrayOutputStream();
    // 4 samples in boundary 5, >100ms
    Span latencySpan100ms1 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions1 = EndSpanOptions.builder().setEndTimestamp(112931232L).build();
    latencySpan100ms1.end(endOptions1);
    Span latencySpan100ms2 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions2 = EndSpanOptions.builder().setEndTimestamp(138694322L).build();
    latencySpan100ms2.end(endOptions2);
    Span latencySpan100ms3 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions3 = EndSpanOptions.builder().setEndTimestamp(154486482L).build();
    latencySpan100ms3.end(endOptions3);
    Span latencySpan100ms4 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions4 = EndSpanOptions.builder().setEndTimestamp(194892582L).build();
    latencySpan100ms4.end(endOptions4);

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(emptyQueryMap, output);

    // Link for boundary 5 with 4 samples
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=5\">4");
  }

  @Test
  public void summaryTable_linkForErrorSpans() {
    OutputStream output = new ByteArrayOutputStream();
    Span errorSpan1 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    Span errorSpan2 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    Span errorSpan3 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    Span finishedSpan = tracer.spanBuilder(FINISHED_SPAN_ONE).startSpan();
    errorSpan1.setStatus(CanonicalCode.CANCELLED.toStatus());
    errorSpan2.setStatus(CanonicalCode.ABORTED.toStatus());
    errorSpan3.setStatus(CanonicalCode.DEADLINE_EXCEEDED.toStatus());
    errorSpan1.end();
    errorSpan2.end();
    errorSpan3.end();
    finishedSpan.end();

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(emptyQueryMap, output);

    // Link for error based spans with 3 samples
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + ERROR_SPAN + "&ztype=2&zsubtype=0\">3");
    // No link for Status{#OK} spans
    assertThat(output.toString())
        .doesNotContain("href=\"?zspanname=" + FINISHED_SPAN_ONE + "&ztype=2&subtype=0\"");
  }

  @Test
  public void spanDetails_emitRunningSpanDetailsCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    Span runningSpan = tracer.spanBuilder(RUNNING_SPAN).startSpan();
    Map<String, String> queryMap =
        ImmutableMap.of("zspanname", RUNNING_SPAN, "ztype", "0", "zsubtype", "0");

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("<h2>Span Details</h2>");
    assertThat(output.toString()).contains("<b> Span Name: " + RUNNING_SPAN + "</b>");
    assertThat(output.toString()).contains("<b> Number of running: 1");
    assertThat(output.toString()).contains(runningSpan.getContext().getTraceId().toLowerBase16());
    assertThat(output.toString()).contains(runningSpan.getContext().getSpanId().toLowerBase16());

    runningSpan.end();
  }

  @Test
  public void spanDetails_emitLatencySpanDetailsCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    Span latencySpan1 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions1 = EndSpanOptions.builder().setEndTimestamp(10002L).build();
    latencySpan1.end(endOptions1);
    Span latencySpan2 = tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L).startSpan();
    EndSpanOptions endOptions2 = EndSpanOptions.builder().setEndTimestamp(10002L).build();
    latencySpan2.end(endOptions2);
    Map<String, String> queryMap =
        ImmutableMap.of("zspanname", LATENCY_SPAN, "ztype", "1", "zsubtype", "1");

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("<h2>Span Details</h2>");
    assertThat(output.toString()).contains("<b> Span Name: " + LATENCY_SPAN + "</b>");
    assertThat(output.toString()).contains("<b> Number of latency samples: 2");
    assertThat(output.toString()).contains(latencySpan1.getContext().getTraceId().toLowerBase16());
    assertThat(output.toString()).contains(latencySpan1.getContext().getSpanId().toLowerBase16());
    assertThat(output.toString()).contains(latencySpan2.getContext().getTraceId().toLowerBase16());
    assertThat(output.toString()).contains(latencySpan2.getContext().getSpanId().toLowerBase16());
  }

  @Test
  public void spanDetails_emitErrorSpanDetailsCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    Span errorSpan1 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    Span errorSpan2 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    errorSpan1.setStatus(CanonicalCode.CANCELLED.toStatus());
    errorSpan2.setStatus(CanonicalCode.ABORTED.toStatus());
    errorSpan1.end();
    errorSpan2.end();
    Map<String, String> queryMap =
        ImmutableMap.of("zspanname", ERROR_SPAN, "ztype", "2", "zsubtype", "0");

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("<h2>Span Details</h2>");
    assertThat(output.toString()).contains("<b> Span Name: " + ERROR_SPAN + "</b>");
    assertThat(output.toString()).contains("<b> Number of error samples: 2");
    assertThat(output.toString()).contains(errorSpan1.getContext().getTraceId().toLowerBase16());
    assertThat(output.toString()).contains(errorSpan1.getContext().getSpanId().toLowerBase16());
    assertThat(output.toString()).contains(errorSpan2.getContext().getTraceId().toLowerBase16());
    assertThat(output.toString()).contains(errorSpan2.getContext().getSpanId().toLowerBase16());
  }

  @Test
  public void spanDetails_shouldNotBreakOnUnknownType() {
    OutputStream output = new ByteArrayOutputStream();
    Map<String, String> queryMap =
        ImmutableMap.of("zspanname", "Span", "ztype", "-1", "zsubtype", "0");

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).doesNotContain("<h2>Span Details</h2>");
    assertThat(output.toString()).doesNotContain("<b> Span Name: Span</b>");
  }
}
