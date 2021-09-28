/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TracezZPageHandlerTest {
  private static final String FINISHED_SPAN_ONE = "FinishedSpanOne";
  private static final String FINISHED_SPAN_TWO = "FinishedSpanTwo";
  private static final String WEIRD_SPAN = "Weird \"' & 1 < 3 \"";
  private static final String RUNNING_SPAN = "RunningSpan";
  private static final String LATENCY_SPAN = "LatencySpan";
  private static final String ERROR_SPAN = "ErrorSpan";
  private static final String EVENT = "event on a span";
  private final TestClock testClock = TestClock.create();
  private final TracezSpanProcessor spanProcessor = TracezSpanProcessor.builder().build();
  private final SdkTracerProvider sdkTracerProvider =
      SdkTracerProvider.builder().setClock(testClock).addSpanProcessor(spanProcessor).build();
  private final Tracer tracer = sdkTracerProvider.get("TracezZPageHandlerTest");
  private final TracezDataAggregator dataAggregator = new TracezDataAggregator(spanProcessor);
  private final Map<String, String> emptyQueryMap = ImmutableMap.of();

  @Test
  void summaryTable_emitRowForEachSpan() {
    OutputStream output = new ByteArrayOutputStream();
    tracer.spanBuilder(FINISHED_SPAN_ONE).startSpan().end();
    tracer.spanBuilder(FINISHED_SPAN_TWO).startSpan().end();
    tracer.spanBuilder(WEIRD_SPAN).startSpan().end();

    Span runningSpan = tracer.spanBuilder(RUNNING_SPAN).startSpan();

    Span latencySpan =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpan.end(10002, TimeUnit.NANOSECONDS);

    Span errorSpan = tracer.spanBuilder(ERROR_SPAN).startSpan();
    errorSpan.setStatus(StatusCode.ERROR);
    errorSpan.end();

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(emptyQueryMap, output);

    String result = output.toString();
    // Emit a row for all types of spans.
    assertThat(result).contains(FINISHED_SPAN_ONE);
    assertThat(result).contains(FINISHED_SPAN_TWO);
    assertThat(result).contains("Weird&quot;&#39; &amp; 1 &lt; 3 &quot;");
    assertThat(result).contains(RUNNING_SPAN);
    assertThat(result).contains(LATENCY_SPAN);
    assertThat(result).contains(ERROR_SPAN);

    runningSpan.end();
  }

  @Test
  void summaryTable_linkForRunningSpans() {
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
  void summaryTable_linkForLatencyBasedSpans_NoneForEmptyBoundary() {
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
  void summaryTable_linkForLatencyBasedSpans_OnePerBoundary() {
    OutputStream output = new ByteArrayOutputStream();
    // Boundary 0, >1us
    Span latencySpanSubtype0 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype0.end(1002, TimeUnit.NANOSECONDS);
    // Boundary 1, >10us
    Span latencySpanSubtype1 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype1.end(10002, TimeUnit.NANOSECONDS);
    // Boundary 2, >100us
    Span latencySpanSubtype2 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype2.end(100002, TimeUnit.NANOSECONDS);
    // Boundary 3, >1ms
    Span latencySpanSubtype3 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype3.end(1000002, TimeUnit.NANOSECONDS);
    // Boundary 4, >10ms
    Span latencySpanSubtype4 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype4.end(10000002, TimeUnit.NANOSECONDS);
    // Boundary 5, >100ms
    Span latencySpanSubtype5 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype5.end(100000002, TimeUnit.NANOSECONDS);
    // Boundary 6, >1s
    Span latencySpanSubtype6 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype6.end(1000000002, TimeUnit.NANOSECONDS);
    // Boundary 7, >10s
    Span latencySpanSubtype7 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype7.end(10000000002L, TimeUnit.NANOSECONDS);
    // Boundary 8, >100s
    Span latencySpanSubtype8 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpanSubtype8.end(100000000002L, TimeUnit.NANOSECONDS);

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
  void summaryTable_linkForLatencyBasedSpans_MultipleForOneBoundary() {
    OutputStream output = new ByteArrayOutputStream();
    // 4 samples in boundary 5, >100ms
    Span latencySpan100ms1 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpan100ms1.end(112931232L, TimeUnit.NANOSECONDS);
    Span latencySpan100ms2 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpan100ms2.end(138694322L, TimeUnit.NANOSECONDS);
    Span latencySpan100ms3 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpan100ms3.end(154486482L, TimeUnit.NANOSECONDS);
    Span latencySpan100ms4 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpan100ms4.end(194892582L, TimeUnit.NANOSECONDS);

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(emptyQueryMap, output);

    // Link for boundary 5 with 4 samples
    assertThat(output.toString())
        .contains("href=\"?zspanname=" + LATENCY_SPAN + "&ztype=1&zsubtype=5\">4");
  }

  @Test
  void summaryTable_linkForErrorSpans() {
    OutputStream output = new ByteArrayOutputStream();
    Span errorSpan1 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    Span errorSpan2 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    Span errorSpan3 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    Span finishedSpan = tracer.spanBuilder(FINISHED_SPAN_ONE).startSpan();
    errorSpan1.setStatus(StatusCode.ERROR, "CANCELLED");
    errorSpan2.setStatus(StatusCode.ERROR, "ABORTED");
    errorSpan3.setStatus(StatusCode.ERROR, "DEADLINE_EXCEEDED");
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
  void spanDetails_emitRunningSpanDetailsCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    Span runningSpan = tracer.spanBuilder(RUNNING_SPAN).startSpan().addEvent(EVENT);
    Map<String, String> queryMap =
        ImmutableMap.of("zspanname", RUNNING_SPAN, "ztype", "0", "zsubtype", "0");

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(queryMap, output);

    String result = output.toString();
    assertThat(result).contains("<h2>Span Details</h2>");
    assertThat(result).contains("<b> Span Name: " + RUNNING_SPAN + "</b>");
    assertThat(result).contains("<b> Number of running: 1");
    assertThat(result).contains(runningSpan.getSpanContext().getTraceId());
    assertThat(result).contains(runningSpan.getSpanContext().getSpanId());
    assertThat(result).contains(EVENT);

    runningSpan.end();
  }

  @Test
  void spanDetails_emitLatencySpanDetailsCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    Span latencySpan1 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpan1.end(10002, TimeUnit.NANOSECONDS);
    Span latencySpan2 =
        tracer.spanBuilder(LATENCY_SPAN).setStartTimestamp(1L, TimeUnit.NANOSECONDS).startSpan();
    latencySpan2.end(10002, TimeUnit.NANOSECONDS);
    Map<String, String> queryMap =
        ImmutableMap.of("zspanname", LATENCY_SPAN, "ztype", "1", "zsubtype", "1");

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("<h2>Span Details</h2>");
    assertThat(output.toString()).contains("<b> Span Name: " + LATENCY_SPAN + "</b>");
    assertThat(output.toString()).contains("<b> Number of latency samples: 2");
    assertThat(output.toString()).contains(latencySpan1.getSpanContext().getTraceId());
    assertThat(output.toString()).contains(latencySpan1.getSpanContext().getSpanId());
    assertThat(output.toString()).contains(latencySpan2.getSpanContext().getTraceId());
    assertThat(output.toString()).contains(latencySpan2.getSpanContext().getSpanId());
  }

  @Test
  void spanDetails_emitErrorSpanDetailsCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    Span errorSpan1 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    Span errorSpan2 = tracer.spanBuilder(ERROR_SPAN).startSpan();
    errorSpan1.setStatus(StatusCode.ERROR, "CANCELLED");
    errorSpan2.setStatus(StatusCode.ERROR, "ABORTED");
    errorSpan1.end();
    errorSpan2.end();
    Map<String, String> queryMap =
        ImmutableMap.of("zspanname", ERROR_SPAN, "ztype", "2", "zsubtype", "0");

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("<h2>Span Details</h2>");
    assertThat(output.toString()).contains("<b> Span Name: " + ERROR_SPAN + "</b>");
    assertThat(output.toString()).contains("<b> Number of error samples: 2");
    assertThat(output.toString()).contains(errorSpan1.getSpanContext().getTraceId());
    assertThat(output.toString()).contains(errorSpan1.getSpanContext().getSpanId());
    assertThat(output.toString()).contains(errorSpan2.getSpanContext().getTraceId());
    assertThat(output.toString()).contains(errorSpan2.getSpanContext().getSpanId());
  }

  @Test
  void spanDetails_shouldNotBreakOnUnknownType() {
    OutputStream output = new ByteArrayOutputStream();
    Map<String, String> queryMap =
        ImmutableMap.of("zspanname", "Span", "ztype", "-1", "zsubtype", "0");

    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);
    tracezZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).doesNotContain("<h2>Span Details</h2>");
    assertThat(output.toString()).doesNotContain("<b> Span Name: Span</b>");
  }

  private static Map<String, String> generateQueryMap(String spanName, String type, String subtype)
      throws URISyntaxException {
    return ZPageHttpHandler.parseQueryString(
        new URI(
                "tracez?zspanname="
                    + urlFormParameterEscaper().escape(spanName)
                    + "&ztype="
                    + type
                    + "&zsubtype="
                    + subtype)
            .getRawQuery());
  }

  @Test
  void spanDetails_emitNameWithSpaceCorrectly()
      throws UnsupportedEncodingException, URISyntaxException {
    OutputStream output = new ByteArrayOutputStream();
    String nameWithSpace = "SPAN NAME";
    Span runningSpan = tracer.spanBuilder(nameWithSpace).startSpan();
    tracer.spanBuilder(nameWithSpace).startSpan().end();
    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);

    tracezZPageHandler.emitHtml(generateQueryMap(nameWithSpace, "0", "0"), output);
    assertThat(output.toString()).contains("<b> Span Name: " + nameWithSpace + "</b>");
    assertThat(output.toString()).contains("<b> Number of running: 1");
    tracezZPageHandler.emitHtml(generateQueryMap(nameWithSpace, "1", "0"), output);
    assertThat(output.toString()).contains("<b> Span Name: " + nameWithSpace + "</b>");
    assertThat(output.toString()).contains("<b> Number of latency samples: 1");

    runningSpan.end();
  }

  @Test
  void spanDetails_emitNameWithPlusCorrectly()
      throws UnsupportedEncodingException, URISyntaxException {
    OutputStream output = new ByteArrayOutputStream();
    String nameWithPlus = "SPAN+NAME";
    Span runningSpan = tracer.spanBuilder(nameWithPlus).startSpan();
    tracer.spanBuilder(nameWithPlus).startSpan().end();
    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);

    tracezZPageHandler.emitHtml(generateQueryMap(nameWithPlus, "0", "0"), output);
    assertThat(output.toString()).contains("<b> Span Name: " + nameWithPlus + "</b>");
    assertThat(output.toString()).contains("<b> Number of running: 1");
    tracezZPageHandler.emitHtml(generateQueryMap(nameWithPlus, "1", "0"), output);
    assertThat(output.toString()).contains("<b> Span Name: " + nameWithPlus + "</b>");
    assertThat(output.toString()).contains("<b> Number of latency samples: 1");

    runningSpan.end();
  }

  @Test
  void spanDetails_emitNamesWithSpaceAndPlusCorrectly()
      throws UnsupportedEncodingException, URISyntaxException {
    OutputStream output = new ByteArrayOutputStream();
    String nameWithSpaceAndPlus = "SPAN + NAME";
    Span runningSpan = tracer.spanBuilder(nameWithSpaceAndPlus).startSpan();
    tracer.spanBuilder(nameWithSpaceAndPlus).startSpan().end();
    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);

    tracezZPageHandler.emitHtml(generateQueryMap(nameWithSpaceAndPlus, "0", "0"), output);
    assertThat(output.toString()).contains("<b> Span Name: " + nameWithSpaceAndPlus + "</b>");
    assertThat(output.toString()).contains("<b> Number of running: 1");
    tracezZPageHandler.emitHtml(generateQueryMap(nameWithSpaceAndPlus, "1", "0"), output);
    assertThat(output.toString()).contains("<b> Span Name: " + nameWithSpaceAndPlus + "</b>");
    assertThat(output.toString()).contains("<b> Number of latency samples: 1");

    runningSpan.end();
  }

  @Test
  void spanDetails_emitNamesWithSpecialUrlCharsCorrectly()
      throws UnsupportedEncodingException, URISyntaxException {
    OutputStream output = new ByteArrayOutputStream();
    String nameWithUrlChars = "{SPAN/NAME}";
    Span runningSpan = tracer.spanBuilder(nameWithUrlChars).startSpan();
    tracer.spanBuilder(nameWithUrlChars).startSpan().end();
    TracezZPageHandler tracezZPageHandler = new TracezZPageHandler(dataAggregator);

    tracezZPageHandler.emitHtml(generateQueryMap(nameWithUrlChars, "0", "0"), output);
    assertThat(output.toString()).contains("<b> Span Name: " + nameWithUrlChars + "</b>");
    assertThat(output.toString()).contains("<b> Number of running: 1");
    tracezZPageHandler.emitHtml(generateQueryMap(nameWithUrlChars, "1", "0"), output);
    assertThat(output.toString()).contains("<b> Span Name: " + nameWithUrlChars + "</b>");
    assertThat(output.toString()).contains("<b> Number of latency samples: 1");

    runningSpan.end();
  }
}
