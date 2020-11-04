/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.zpages;

import static com.google.common.html.HtmlEscapers.htmlEscaper;
import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
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
import java.util.logging.Level;
import java.util.logging.Logger;
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
  private static final String TRACEZ_NAME = "TraceZ";
  private static final String TRACEZ_DESCRIPTION =
      "TraceZ displays information about all the running spans"
          + " and all the sampled spans based on latency and errors";
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
  // Map from LatencyBoundary to human readable string on the UI
  private static final ImmutableMap<LatencyBoundary, String> LATENCY_BOUNDARIES_STRING_MAP =
      buildLatencyBoundaryStringMap();
  private static final Logger logger = Logger.getLogger(TracezZPageHandler.class.getName());
  @Nullable private final TracezDataAggregator dataAggregator;

  /** Constructs a new {@code TracezZPageHandler}. */
  TracezZPageHandler(@Nullable TracezDataAggregator dataAggregator) {
    this.dataAggregator = dataAggregator;
  }

  @Override
  public String getUrlPath() {
    return TRACEZ_URL;
  }

  @Override
  public String getPageName() {
    return TRACEZ_NAME;
  }

  @Override
  public String getPageDescription() {
    return TRACEZ_DESCRIPTION;
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
   */
  private static void emitSummaryTableHeader(PrintStream out) {
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
    for (LatencyBoundary latencyBoundary : LatencyBoundary.values()) {
      out.print(
          "<th colspan=1 class=\"border-left-white align-center\""
              + "style=\"color: #fff;\"><b>["
              + LATENCY_BOUNDARIES_STRING_MAP.get(latencyBoundary)
              + "]</b></th>");
    }
    out.print("<th colspan=1 class=\"border-left-white\"></th>");
    out.print("</tr>");
  }

  /**
   * Emits a single cell of the summary table depends on the paramters passed in, to the {@link
   * PrintStream} {@code out}.
   *
   * @param out the {@link PrintStream} {@code out}.
   * @param spanName the name of the corresponding span.
   * @param numOfSamples the number of samples of the corresponding span.
   * @param type the type of the corresponding span (running, latency, error).
   * @param subtype the sub-type of the corresponding span (latency [0, 8], error [0, 15]).
   */
  private static void emitSummaryTableCell(
      PrintStream out, String spanName, int numOfSamples, SampleType type, int subtype) {
    // If numOfSamples is greater than 0, emit a link to see detailed span information
    // If numOfSamples is smaller than 0, print the text "N/A", otherwise print the text "0"
    if (numOfSamples > 0) {
      out.print("<td class=\"align-center border-left-dark\"><a href=\"?");
      out.print(PARAM_SPAN_NAME + "=" + urlFormParameterEscaper().escape(spanName));
      out.print("&" + PARAM_SAMPLE_TYPE + "=" + type.getValue());
      out.print("&" + PARAM_SAMPLE_SUB_TYPE + "=" + subtype);
      out.print("\">" + numOfSamples + "</a></td>");
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
   */
  private void emitSummaryTable(PrintStream out) {
    if (dataAggregator == null) {
      return;
    }
    out.print("<table style=\"border-spacing: 0; border: 1px solid #363636;\">");
    emitSummaryTableHeader(out);

    Set<String> spanNames = dataAggregator.getSpanNames();
    boolean zebraStripe = false;

    Map<String, Integer> runningSpanCounts = dataAggregator.getRunningSpanCounts();
    Map<String, Map<LatencyBoundary, Integer>> latencySpanCounts =
        dataAggregator.getSpanLatencyCounts();
    Map<String, Integer> errorSpanCounts = dataAggregator.getErrorSpanCounts();
    for (String spanName : spanNames) {
      if (zebraStripe) {
        out.print("<tr style=\"background-color: " + ZEBRA_STRIPE_COLOR + "\">");
      } else {
        out.print("<tr>");
      }
      zebraStripe = !zebraStripe;
      out.print("<td>" + htmlEscaper().escape(spanName) + "</td>");

      // Running spans column
      int numOfRunningSpans =
          runningSpanCounts.containsKey(spanName) ? runningSpanCounts.get(spanName) : 0;
      // subtype is ignored for running spans
      emitSummaryTableCell(out, spanName, numOfRunningSpans, SampleType.RUNNING, 0);

      // Latency based sampled spans column
      int subtype = 0;
      for (LatencyBoundary latencyBoundary : LatencyBoundary.values()) {
        int numOfLatencySamples =
            latencySpanCounts.containsKey(spanName)
                    && latencySpanCounts.get(spanName).containsKey(latencyBoundary)
                ? latencySpanCounts.get(spanName).get(latencyBoundary)
                : 0;
        emitSummaryTableCell(out, spanName, numOfLatencySamples, SampleType.LATENCY, subtype);
        subtype += 1;
      }

      // Error based sampled spans column
      int numOfErrorSamples =
          errorSpanCounts.containsKey(spanName) ? errorSpanCounts.get(spanName) : 0;
      // subtype 0 means all errors
      emitSummaryTableCell(out, spanName, numOfErrorSamples, SampleType.ERROR, 0);
    }
    out.print("</table>");
  }

  private static void emitSpanNameAndCount(
      PrintStream out, String spanName, int count, SampleType type) {
    out.print(
        "<p class=\"align-center\"><b> Span Name: " + htmlEscaper().escape(spanName) + "</b></p>");
    String typeString =
        type == SampleType.RUNNING
            ? "running"
            : type == SampleType.LATENCY ? "latency samples" : "error samples";
    out.print("<p class=\"align-center\"><b> Number of " + typeString + ": " + count + "</b></p>");
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
        span.hasEnded()
            ? String.format("%.6f", (span.getEndEpochNanos() - span.getStartEpochNanos()) * 1.0e-9)
            : "";
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
        span.isSampled() ? SAMPLED_TRACE_ID_COLOR : NOT_SAMPLED_TRACE_ID_COLOR,
        span.getTraceId(),
        span.getSpanId(),
        (span.getParentSpanId() == null ? SpanId.getInvalid() : span.getParentSpanId()));
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
    SpanData.Status status = span.getStatus();
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
    attributes.forEach(
        new AttributeConsumer() {
          private boolean first = true;

          @Override
          public <T> void accept(AttributeKey<T> key, T value) {
            if (first) {
              first = false;
            } else {
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
  private void emitHtmlBody(Map<String, String> queryMap, PrintStream out) {
    if (dataAggregator == null) {
      out.print("OpenTelemetry implementation not available.");
      return;
    }
    out.print(
        "<a href=\"/\"><img style=\"height: 90px;\" src=\"data:image/png;base64,"
            + ZPageLogo.getLogoBase64()
            + "\" /></a>");
    out.print("<h1>TraceZ Summary</h1>");
    emitSummaryTable(out);
    // spanName will be null if the query parameter doesn't exist in the URL
    String spanName = queryMap.get(PARAM_SPAN_NAME);
    if (spanName != null) {
      // Show detailed information for the corresponding span
      String typeStr = queryMap.get(PARAM_SAMPLE_TYPE);
      if (typeStr != null) {
        List<SpanData> spans = null;
        SampleType type = SampleType.fromString(typeStr);
        if (type == SampleType.UNKNOWN) {
          // Type of unknown is garbage value
          return;
        } else if (type == SampleType.RUNNING) {
          // Display running span
          spans = dataAggregator.getRunningSpans(spanName);
        } else {
          String subtypeStr = queryMap.get(PARAM_SAMPLE_SUB_TYPE);
          if (subtypeStr != null) {
            int subtype = Integer.parseInt(subtypeStr);
            if (type == SampleType.LATENCY) {
              if (subtype < 0 || subtype >= LatencyBoundary.values().length) {
                // N/A or out-of-bound check for latency based subtype, valid values: [0, 8]
                return;
              }
              // Display latency based span
              LatencyBoundary latencyBoundary = LatencyBoundary.values()[subtype];
              spans =
                  dataAggregator.getOkSpans(
                      spanName,
                      latencyBoundary.getLatencyLowerBound(),
                      latencyBoundary.getLatencyUpperBound());
            } else {
              if (subtype < 0 || subtype >= StatusCode.values().length) {
                // N/A or out-of-bound cueck for error based subtype, valid values: [0, 15]
                return;
              }
              // Display error based span
              spans = dataAggregator.getErrorSpans(spanName);
            }
          }
        }
        out.print("<h2>Span Details</h2>");
        emitSpanNameAndCount(out, spanName, spans == null ? 0 : spans.size(), type);

        if (spans != null) {
          Formatter formatter = new Formatter(out, Locale.US);
          spans =
              ImmutableList.sortedCopyOf(new SpanDataComparator(/* incremental= */ true), spans);
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
              + ZPageLogo.getFaviconBase64()
              + "\" type=\"image/png\">");
      out.print(
          "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300\""
              + "rel=\"stylesheet\">");
      out.print(
          "<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">");
      out.print("<title>" + TRACEZ_NAME + "</title>");
      emitHtmlStyle(out);
      out.print("</head>");
      out.print("<body>");
      try {
        emitHtmlBody(queryMap, out);
      } catch (Throwable t) {
        out.print("Error while generating HTML: " + t.toString());
        logger.log(Level.WARNING, "error while generating HTML", t);
      }
      out.print("</body>");
      out.print("</html>");
    } catch (Throwable t) {
      logger.log(Level.WARNING, "error while generating HTML", t);
    }
  }

  private static String latencyBoundaryToString(LatencyBoundary latencyBoundary) {
    switch (latencyBoundary) {
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
    throw new IllegalArgumentException("No value string available for: " + latencyBoundary);
  }

  private static ImmutableMap<LatencyBoundary, String> buildLatencyBoundaryStringMap() {
    Map<LatencyBoundary, String> latencyBoundaryMap = new HashMap<>();
    for (LatencyBoundary latencyBoundary : LatencyBoundary.values()) {
      latencyBoundaryMap.put(latencyBoundary, latencyBoundaryToString(latencyBoundary));
    }
    return ImmutableMap.copyOf(latencyBoundaryMap);
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
