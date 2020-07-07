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

import static com.google.common.html.HtmlEscapers.htmlEscaper;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.common.ReadableKeyValuePairs.KeyValueConsumer;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Status.CanonicalCode;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

final class TracezZPageHandler extends ZPageHandler {
  private enum SampleType {
    RUNNING(0),
    LATENCY(1),
    ERROR(2),
    UNKNOWN(-1);

    private final int value;

    SampleType(int value) {
      this.value = value;
    }

    static SampleType fromString(String str) {
      int value = Integer.parseInt(str);
      switch (value) {
        case 0:
          return RUNNING;
        case 1:
          return LATENCY;
        case 2:
          return ERROR;
        default:
          return UNKNOWN;
      }
    }

    int getValue() {
      return value;
    }
  }

  private static final String TRACEZ_URL = "/tracez";
  // Background color used for zebra striping rows of summary table
  private static final String ZEBRA_STRIPE_COLOR = "#e6e6e6";
  // Color for sampled traceIds
  private static final String SAMPLED_TRACE_ID_COLOR = "#c1272d";
  // Color for not sampled traceIds
  private static final String NOT_SAMPLED_TRACE_ID_COLOR = "black";
  // Query string parameter name for span name
  private static final String PARAM_SPAN_NAME = "zspanname";
  // Query string parameter name for type to display
  // * 0 = running, 1 = latency, 2 = error
  private static final String PARAM_SAMPLE_TYPE = "ztype";
  // Query string parameter name for sub-type:
  // * for latency based sampled spans [0, 8] corresponds to each latency boundaries
  //   where 0 corresponds to the first boundary
  // * for error based sampled spans [0, 15], 0 means all, otherwise the error code
  private static final String PARAM_SAMPLE_SUB_TYPE = "zsubtype";
  // Map from LatencyBoundaries to human readable string on the UI
  private static final ImmutableMap<LatencyBoundaries, String> LATENCY_BOUNDARIES_STRING_MAP =
      buildLatencyBoundariesStringMap();
  @Nullable private final TracezDataAggregator dataAggregator;

  /** Constructs a new {@code TracezZPageHandler}. */
  TracezZPageHandler(@Nullable TracezDataAggregator dataAggregator) {
    this.dataAggregator = dataAggregator;
  }

  @Override
  public String getUrlPath() {
    return TRACEZ_URL;
  }

  /**
   * Emits CSS Styles to the {@link PrintStream} {@code out}. Content emitted by this function
   * should be enclosed by <head></head> tag.
   *
   * @param out the {@link PrintStream} {@code out}.
   */
  private static void emitHtmlStyle(PrintStream out) {
    out.print("<style>");
    out.print(ZPageStyle.style);
    out.print("</style>");
  }

  /**
   * Emits the header of the summary table to the {@link PrintStream} {@code out}.
   *
   * @param out the {@link PrintStream} {@code out}.
   * @param formatter a {@link Formatter} for formatting HTML expressions.
   */
  private static void emitSummaryTableHeader(PrintStream out, Formatter formatter) {
    // First row
    out.print("<tr class=\"bg-color\">");
    out.print("<th colspan=1 class=\"header-text\"><b>Span Name</b></th>");
    out.print("<th colspan=1 class=\"header-text border-left-white\"><b>Running</b></th>");
    out.print("<th colspan=9 class=\"header-text border-left-white\"><b>Latency Samples</b></th>");
    out.print("<th colspan=1 class=\"header-text border-left-white\"><b>Error Samples</b></th>");
    out.print("</tr>");

    // Second row
    out.print("<tr class=\"bg-color\">");
    out.print("<th colspan=1></th>");
    out.print("<th colspan=1 class=\"border-left-white\"></th>");
    for (LatencyBoundaries latencyBoundaries : LatencyBoundaries.values()) {
      formatter.format(
          "<th colspan=1 class=\"border-left-white align-center\""
              + "style=\"color: #fff;\"><b>[%s]</b></th>",
          LATENCY_BOUNDARIES_STRING_MAP.get(latencyBoundaries));
    }
    out.print("<th colspan=1 class=\"border-left-white\"></th>");
    out.print("</tr>");
  }

  /**
   * Emits a single cell of the summary table depends on the paramters passed in, to the {@link
   * PrintStream} {@code out}.
   *
   * @param out the {@link PrintStream} {@code out}.
   * @param formatter the {@link Formatter} for formatting HTML expressions.
   * @param spanName the name of the corresponding span.
   * @param numOfSamples the number of samples of the corresponding span.
   * @param type the type of the corresponding span (running, latency, error).
   * @param subtype the sub-type of the corresponding span (latency [0, 8], error [0, 15]).
   */
  private static void emitSummaryTableCell(
      PrintStream out,
      Formatter formatter,
      String spanName,
      int numOfSamples,
      SampleType type,
      int subtype)
      throws UnsupportedEncodingException {
    // If numOfSamples is greater than 0, emit a link to see detailed span information
    // If numOfSamples is smaller than 0, print the text "N/A", otherwise print the text "0"
    if (numOfSamples > 0) {
      formatter.format(
          "<td class=\"align-center border-left-dark\"><a href=\"?%s=%s&%s=%d&%s=%d\">%d</a></td>",
          PARAM_SPAN_NAME,
          URLEncoder.encode(spanName, "UTF-8"),
          PARAM_SAMPLE_TYPE,
          type.getValue(),
          PARAM_SAMPLE_SUB_TYPE,
          subtype,
          numOfSamples);
    } else if (numOfSamples < 0) {
      out.print("<td class=\"align-center border-left-dark\">N/A</td>");
    } else {
      out.print("<td class=\"align-center border-left-dark\">0</td>");
    }
  }

  /**
   * Emits the summary table of running spans and sampled spans to the {@link PrintStream} {@code
   * out}.
   *
   * @param out the {@link PrintStream} {@code out}.
   * @param formatter a {@link Formatter} for formatting HTML expressions.
   */
  private void emitSummaryTable(PrintStream out, Formatter formatter)
      throws UnsupportedEncodingException {
    if (dataAggregator == null) {
      return;
    }
    out.print("<table style=\"border-spacing: 0; border: 1px solid #363636;\">");
    emitSummaryTableHeader(out, formatter);

    Set<String> spanNames = dataAggregator.getSpanNames();
    boolean zebraStripe = false;

    Map<String, Integer> runningSpanCounts = dataAggregator.getRunningSpanCounts();
    Map<String, Map<LatencyBoundaries, Integer>> latencySpanCounts =
        dataAggregator.getSpanLatencyCounts();
    Map<String, Integer> errorSpanCounts = dataAggregator.getErrorSpanCounts();
    for (String spanName : spanNames) {
      if (zebraStripe) {
        formatter.format("<tr style=\"background-color: %s\">", ZEBRA_STRIPE_COLOR);
      } else {
        out.print("<tr>");
      }
      zebraStripe = !zebraStripe;
      formatter.format("<td>%s</td>", htmlEscaper().escape(spanName));

      // Running spans column
      int numOfRunningSpans =
          runningSpanCounts.containsKey(spanName) ? runningSpanCounts.get(spanName) : 0;
      // subtype is ignored for running spans
      emitSummaryTableCell(out, formatter, spanName, numOfRunningSpans, SampleType.RUNNING, 0);

      // Latency based sampled spans column
      int subtype = 0;
      for (LatencyBoundaries latencyBoundaries : LatencyBoundaries.values()) {
        int numOfLatencySamples =
            latencySpanCounts.containsKey(spanName)
                    && latencySpanCounts.get(spanName).containsKey(latencyBoundaries)
                ? latencySpanCounts.get(spanName).get(latencyBoundaries)
                : 0;
        emitSummaryTableCell(
            out, formatter, spanName, numOfLatencySamples, SampleType.LATENCY, subtype);
        subtype += 1;
      }

      // Error based sampled spans column
      int numOfErrorSamples =
          errorSpanCounts.containsKey(spanName) ? errorSpanCounts.get(spanName) : 0;
      // subtype 0 means all errors
      emitSummaryTableCell(out, formatter, spanName, numOfErrorSamples, SampleType.ERROR, 0);
    }
    out.print("</table>");
  }

  private static void emitSpanNameAndCount(
      Formatter formatter, String spanName, int count, SampleType type) {
    formatter.format(
        "<p class=\"align-center\"><b> Span Name: %s </b></p>", htmlEscaper().escape(spanName));
    formatter.format(
        "<p class=\"align-center\"><b> Number of %s: %d </b></p>",
        type == SampleType.RUNNING
            ? "running"
            : type == SampleType.LATENCY ? "latency samples" : "error samples",
        count);
  }

  private static void emitSpanDetails(
      PrintStream out, Formatter formatter, Collection<SpanData> spans) {
    out.print("<table style=\"border-spacing: 0; border: 1px solid #363636;\">");
    out.print("<tr class=\"bg-color\">");
    out.print(
        "<td style=\"color: #fff;\"><pre class=\"no-margin wrap-text\"><b>When</b></pre></td>");
    out.print(
        "<td class=\"border-left-white\" style=\"color: #fff;\">"
            + "<pre class=\"no-margin wrap-text\"><b>Elapsed(s)</b></pre></td>");
    out.print("<td class=\"border-left-white\"></td>");
    out.print("</tr>");
    boolean zebraStripe = false;
    for (SpanData span : spans) {
      zebraStripe = emitSingleSpan(out, formatter, span, zebraStripe);
    }
    out.print("</table>");
  }

  private static boolean emitSingleSpan(
      PrintStream out, Formatter formatter, SpanData span, boolean zebraStripe) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(TimeUnit.NANOSECONDS.toMillis(span.getStartEpochNanos()));
    long microsField = TimeUnit.NANOSECONDS.toMicros(span.getStartEpochNanos());
    String elapsedSecondsStr =
        span.getHasEnded()
            ? String.format("%.6f", (span.getEndEpochNanos() - span.getStartEpochNanos()) * 1.0e-9)
            : String.format("%s", "");
    formatter.format(
        "<tr style=\"background-color: %s;\">", zebraStripe ? ZEBRA_STRIPE_COLOR : "#fff");
    formatter.format(
        "<td class=\"align-right\"><pre class=\"no-margin wrap-text\"><b>"
            + "%04d/%02d/%02d-%02d:%02d:%02d.%06d</b></pre></td>",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND),
        microsField);
    formatter.format(
        "<td class=\"border-left-dark\"><pre class=\"no-margin wrap-text\"><b>%s</b></pre></td>",
        elapsedSecondsStr);
    formatter.format(
        "<td class=\"border-left-dark\"><pre class=\"no-margin wrap-text\"><b>"
            + "TraceId: <b style=\"color:%s;\">%s</b> "
            + " | SpanId: %s | ParentSpanId: %s</b></pre></td>",
        span.getTraceFlags().isSampled() ? SAMPLED_TRACE_ID_COLOR : NOT_SAMPLED_TRACE_ID_COLOR,
        span.getTraceId().toLowerBase16(),
        span.getSpanId().toLowerBase16(),
        (span.getParentSpanId() == null
            ? SpanId.getInvalid().toLowerBase16()
            : span.getParentSpanId().toLowerBase16()));
    out.print("</tr>");
    zebraStripe = !zebraStripe;

    int lastEntryDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

    long lastEpochNanos = span.getStartEpochNanos();
    List<Event> timedEvents = new ArrayList<>(span.getEvents());
    Collections.sort(timedEvents, new EventComparator());
    for (Event event : timedEvents) {
      calendar.setTimeInMillis(TimeUnit.NANOSECONDS.toMillis(event.getEpochNanos()));
      formatter.format(
          "<tr style=\"background-color: %s;\">", zebraStripe ? ZEBRA_STRIPE_COLOR : "#fff");
      emitSingleEvent(out, formatter, event, calendar, lastEntryDayOfYear, lastEpochNanos);
      out.print("</tr>");
      if (calendar.get(Calendar.DAY_OF_YEAR) != lastEntryDayOfYear) {
        lastEntryDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
      }
      lastEpochNanos = event.getEpochNanos();
      zebraStripe = !zebraStripe;
    }
    formatter.format(
        "<tr style=\"background-color: %s;\"><td></td><td class=\"border-left-dark\">"
            + "</td><td class=\"border-left-dark\"><pre class=\"no-margin wrap-text\">",
        zebraStripe ? ZEBRA_STRIPE_COLOR : "#fff");
    Status status = span.getStatus();
    if (status != null) {
      formatter.format("%s | ", htmlEscaper().escape(status.toString()));
    }
    formatter.format("%s</pre></td>", htmlEscaper().escape(renderAttributes(span.getAttributes())));
    zebraStripe = !zebraStripe;
    return zebraStripe;
  }

  private static void emitSingleEvent(
      PrintStream out,
      Formatter formatter,
      Event event,
      Calendar calendar,
      int lastEntryDayOfYear,
      long lastEpochNanos) {
    if (calendar.get(Calendar.DAY_OF_YEAR) == lastEntryDayOfYear) {
      out.print("<td class=\"align-right\"><pre class=\"no-margin wrap-text\">");
    } else {
      formatter.format(
          "<td class=\"align-right\"><pre class=\"no-margin wrap-text\">%04d/%02d/%02d-",
          calendar.get(Calendar.YEAR),
          calendar.get(Calendar.MONTH) + 1,
          calendar.get(Calendar.DAY_OF_MONTH));
    }

    // Special printing so that durations smaller than one second
    // are left padded with blanks instead of '0' characters.
    // E.g.,
    //        Number                  Printout
    //        ---------------------------------
    //        0.000534                  .   534
    //        1.000534                 1.000534
    long deltaMicros = TimeUnit.NANOSECONDS.toMicros(event.getEpochNanos() - lastEpochNanos);
    String deltaString;
    if (deltaMicros >= 1000000) {
      deltaString = String.format("%.6f", (deltaMicros / 1000000.0));
    } else {
      deltaString = String.format("%1s.%6d", "", deltaMicros);
    }

    long microsField = TimeUnit.NANOSECONDS.toMicros(event.getEpochNanos());
    formatter.format(
        "%02d:%02d:%02d.%06d</pre></td> "
            + "<td class=\"border-left-dark\"><pre class=\"no-margin wrap-text\">%s</pre></td>"
            + "<td class=\"border-left-dark\"><pre class=\"no-margin wrap-text\">%s</pre></td>",
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND),
        microsField,
        deltaString,
        htmlEscaper().escape(renderEvent(event)));
  }

  private static String renderAttributes(ReadableAttributes attributes) {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Attributes:{");
    final AtomicBoolean first = new AtomicBoolean(true);
    attributes.forEach(
        new KeyValueConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
            if (!first.getAndSet(false)) {
              stringBuilder.append(", ");
            }
            stringBuilder.append(key);
            stringBuilder.append("=");
            stringBuilder.append(value.toString());
          }
        });
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

  private static String renderEvent(Event event) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(event.getName());
    if (!event.getAttributes().isEmpty()) {
      stringBuilder.append(" | ");
      stringBuilder.append(renderAttributes(event.getAttributes()));
    }
    return stringBuilder.toString();
  }

  /**
   * Emits HTML body content to the {@link PrintStream} {@code out}. Content emitted by this
   * function should be enclosed by <body></body> tag.
   *
   * @param queryMap the map containing URL query parameters.s
   * @param out the {@link PrintStream} {@code out}.
   */
  private void emitHtmlBody(Map<String, String> queryMap, PrintStream out)
      throws UnsupportedEncodingException {
    if (dataAggregator == null) {
      out.print("OpenTelemetry implementation not available.");
      return;
    }
    // Link to OpenTelemetry Logo
    out.print(
        "<img style=\"height: 90px;\" src=\"data:image/png;base64,"
            + ZPageLogo.logoBase64
            + "\" />");
    out.print("<h1>TraceZ Summary</h1>");
    Formatter formatter = new Formatter(out, Locale.US);
    emitSummaryTable(out, formatter);
    // spanName will be null if the query parameter doesn't exist in the URL
    String spanName = queryMap.get(PARAM_SPAN_NAME);
    if (spanName != null) {
      // Convert spanName with URL encoding
      spanName = URLEncoder.encode(spanName, "UTF-8");
      // Show detailed information for the corresponding span
      String typeStr = queryMap.get(PARAM_SAMPLE_TYPE);
      if (typeStr != null) {
        List<SpanData> spans = null;
        SampleType type = SampleType.fromString(typeStr);
        if (type == SampleType.UNKNOWN) {
          // Type of sample is garbage value
          return;
        } else if (type == SampleType.RUNNING) {
          // Display running span
          spans = dataAggregator.getRunningSpans(spanName);
          Collections.sort(spans, new SpanDataComparator(/* incremental= */ true));
        } else {
          String subtypeStr = queryMap.get(PARAM_SAMPLE_SUB_TYPE);
          if (subtypeStr != null) {
            int subtype = Integer.parseInt(subtypeStr);
            if (type == SampleType.LATENCY) {
              if (subtype < 0 || subtype >= LatencyBoundaries.values().length) {
                // N/A or out-of-bound check for latency based subtype, valid values: [0, 8]
                return;
              }
              // Display latency based span
              LatencyBoundaries latencyBoundary = LatencyBoundaries.values()[subtype];
              spans =
                  dataAggregator.getOkSpans(
                      spanName,
                      latencyBoundary.getLatencyLowerBound(),
                      latencyBoundary.getLatencyUpperBound());
              Collections.sort(spans, new SpanDataComparator(/* incremental= */ false));
            } else {
              if (subtype < 0 || subtype >= CanonicalCode.values().length) {
                // N/A or out-of-bound cueck for error based subtype, valid values: [0, 15]
                return;
              }
              // Display error based span
              spans = dataAggregator.getErrorSpans(spanName);
              Collections.sort(spans, new SpanDataComparator(/* incremental= */ false));
            }
          }
        }
        out.print("<h2>Span Details</h2>");
        emitSpanNameAndCount(formatter, spanName, spans == null ? 0 : spans.size(), type);

        if (spans != null) {
          emitSpanDetails(out, formatter, spans);
        }
      }
    }
  }

  @Override
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    // PrintStream for emiting HTML contents
    try (PrintStream out = new PrintStream(outputStream, /* autoFlush= */ false, "UTF-8")) {
      out.print("<!DOCTYPE html>");
      out.print("<html lang=\"en\">");
      out.print("<head>");
      out.print("<meta charset=\"UTF-8\">");
      out.print(
          "<link rel=\"shortcut icon\" href=\"data:image/png;base64,"
              + ZPageLogo.faviconBase64
              + "\" type=\"image/png\">");
      out.print(
          "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300\""
              + "rel=\"stylesheet\">");
      out.print(
          "<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">");
      out.print("<title>TraceZ</title>");
      emitHtmlStyle(out);
      out.print("</head>");
      out.print("<body>");
      try {
        emitHtmlBody(queryMap, out);
      } catch (Throwable t) {
        out.print("Error while generating HTML: " + t.toString());
      }
      out.print("</body>");
      out.print("</html>");
    } catch (Throwable t) {
      System.err.print("Error while generating HTML: " + t.toString());
    }
  }

  private static String latencyBoundariesToString(LatencyBoundaries latencyBoundaries) {
    switch (latencyBoundaries) {
      case ZERO_MICROSx10:
        return ">0us";
      case MICROSx10_MICROSx100:
        return ">10us";
      case MICROSx100_MILLIx1:
        return ">100us";
      case MILLIx1_MILLIx10:
        return ">1ms";
      case MILLIx10_MILLIx100:
        return ">10ms";
      case MILLIx100_SECONDx1:
        return ">100ms";
      case SECONDx1_SECONDx10:
        return ">1s";
      case SECONDx10_SECONDx100:
        return ">10s";
      case SECONDx100_MAX:
        return ">100s";
    }
    throw new IllegalArgumentException("No value string available for: " + latencyBoundaries);
  }

  private static ImmutableMap<LatencyBoundaries, String> buildLatencyBoundariesStringMap() {
    Map<LatencyBoundaries, String> latencyBoundariesMap = new HashMap<>();
    for (LatencyBoundaries latencyBoundaries : LatencyBoundaries.values()) {
      latencyBoundariesMap.put(latencyBoundaries, latencyBoundariesToString(latencyBoundaries));
    }
    return ImmutableMap.copyOf(latencyBoundariesMap);
  }

  private static final class EventComparator implements Comparator<Event>, Serializable {
    private static final long serialVersionUID = 0;

    @Override
    public int compare(Event e1, Event e2) {
      return Long.compare(e1.getEpochNanos(), e2.getEpochNanos());
    }
  }

  private static final class SpanDataComparator implements Comparator<SpanData>, Serializable {
    private static final long serialVersionUID = 0;
    private final boolean incremental;

    /**
     * Returns a new {@code SpanDataComparator}.
     *
     * @param incremental {@code true} if sorting spans incrementally
     */
    private SpanDataComparator(boolean incremental) {
      this.incremental = incremental;
    }

    @Override
    public int compare(SpanData s1, SpanData s2) {
      return incremental
          ? Long.compare(s1.getStartEpochNanos(), s2.getStartEpochNanos())
          : Long.compare(s2.getStartEpochNanos(), s1.getEndEpochNanos());
    }
  }
}
