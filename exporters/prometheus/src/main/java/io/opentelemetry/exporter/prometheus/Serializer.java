/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:

/*
 * Prometheus instrumentation library for JVM applications
 * Copyright 2012-2015 The Prometheus Authors
 *
 * This product includes software developed at
 * Boxever Ltd. (http://www.boxever.com/).
 *
 * This product includes software developed at
 * SoundCloud Ltd. (http://soundcloud.com/).
 *
 * This product includes software developed as part of the
 * Ocelli project by Netflix Inc. (https://github.com/Netflix/ocelli/).
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** Serializes metrics into Prometheus exposition formats. */
// Adapted from
// https://github.com/prometheus/client_java/blob/master/simpleclient_common/src/main/java/io/prometheus/client/exporter/common/TextFormat.java
abstract class Serializer {

  static Serializer create(@Nullable String acceptHeader, Predicate<String> filter) {
    if (acceptHeader == null) {
      return new Prometheus004Serializer(filter);
    }

    for (String accepts : acceptHeader.split(",")) {
      if ("application/openmetrics-text".equals(accepts.split(";")[0].trim())) {
        return new OpenMetrics100Serializer(filter);
      }
    }

    return new Prometheus004Serializer(filter);
  }

  private final Predicate<String> metricNameFilter;

  Serializer(Predicate<String> metricNameFilter) {
    this.metricNameFilter = metricNameFilter;
  }

  abstract String contentType();

  abstract String headerName(String name, PrometheusType type);

  abstract void writeHelp(Writer writer, String description) throws IOException;

  abstract void writeTimestamp(Writer writer, long timestampNanos) throws IOException;

  abstract void writeExemplar(
      Writer writer, Collection<ExemplarData> exemplars, double minExemplar, double maxExemplar)
      throws IOException;

  abstract void writeEof(Writer writer) throws IOException;

  final void write(Collection<MetricData> metrics, OutputStream output) throws IOException {
    try (Writer writer =
        new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
      for (MetricData metric : metrics) {
        write(metric, writer);
      }
      writeEof(writer);
    }
  }

  private void write(MetricData metric, Writer writer) throws IOException {
    // Not supported in specification yet.
    if (metric.getType() == MetricDataType.EXPONENTIAL_HISTOGRAM) {
      return;
    }

    PrometheusType type = PrometheusType.forMetric(metric);
    String name = NameSanitizer.INSTANCE.apply(metric.getName());
    String headerName = headerName(name, type);
    if (type == PrometheusType.COUNTER) {
      name = name + "_total";
    }

    if (!metricNameFilter.test(name)) {
      return;
    }

    writer.write("# TYPE ");
    writer.write(headerName);
    writer.write(' ');
    writer.write(type.getTypeString());
    writer.write('\n');

    writer.write("# HELP ");
    writer.write(headerName);
    writer.write(' ');
    writeHelp(writer, metric.getDescription());
    writer.write('\n');

    for (PointData point : MetricAdapter.getPoints(metric)) {
      switch (metric.getType()) {
        case DOUBLE_SUM:
        case DOUBLE_GAUGE:
          writePoint(
              writer,
              name,
              ((DoublePointData) point).getValue(),
              point.getAttributes(),
              point.getEpochNanos());
          break;
        case LONG_SUM:
        case LONG_GAUGE:
          writePoint(
              writer,
              name,
              ((LongPointData) point).getValue(),
              point.getAttributes(),
              point.getEpochNanos());
          break;
        case HISTOGRAM:
          writeHistogram(writer, name, (DoubleHistogramPointData) point);
          break;
        case SUMMARY:
          writeSummary(writer, name, (SummaryPointData) point);
          break;
        case EXPONENTIAL_HISTOGRAM:
          throw new IllegalArgumentException("Can't happen");
      }
    }
  }

  private void writeHistogram(Writer writer, String name, DoubleHistogramPointData point)
      throws IOException {
    writePoint(
        writer, name + "_count", point.getCount(), point.getAttributes(), point.getEpochNanos());
    writePoint(writer, name + "_sum", point.getSum(), point.getAttributes(), point.getEpochNanos());

    long cumulativeCount = 0;
    List<Long> counts = point.getCounts();
    for (int i = 0; i < counts.size(); i++) {
      // This is the upper boundary (inclusive). I.e. all values should be < this value (LE -
      // Less-then-or-Equal).
      double boundary = point.getBucketUpperBound(i);

      cumulativeCount += counts.get(i);
      writePoint(
          writer,
          name + "_bucket",
          cumulativeCount,
          point.getAttributes(),
          point.getEpochNanos(),
          "le",
          boundary,
          point.getExemplars(),
          point.getBucketLowerBound(i),
          boundary);
    }
  }

  private void writeSummary(Writer writer, String name, SummaryPointData point) throws IOException {
    writePoint(
        writer, name + "_count", point.getCount(), point.getAttributes(), point.getEpochNanos());
    writePoint(writer, name + "_sum", point.getSum(), point.getAttributes(), point.getEpochNanos());

    List<ValueAtPercentile> valueAtPercentiles = point.getPercentileValues();
    for (ValueAtPercentile valueAtPercentile : valueAtPercentiles) {
      writePoint(
          writer,
          name,
          valueAtPercentile.getValue(),
          point.getAttributes(),
          point.getEpochNanos(),
          "quantile",
          valueAtPercentile.getPercentile(),
          Collections.emptyList(),
          0,
          0);
    }
  }

  private void writePoint(
      Writer writer, String name, double value, Attributes attributes, long epochNanos)
      throws IOException {
    writer.write(name);
    writeAttributes(writer, attributes);
    writer.write(' ');
    writeDouble(writer, value);
    writer.write(' ');
    writeTimestamp(writer, epochNanos);
    writer.write('\n');
  }

  private void writePoint(
      Writer writer,
      String name,
      double value,
      Attributes attributes,
      long epochNanos,
      String additionalAttrKey,
      double additionalAttrValue,
      Collection<ExemplarData> exemplars,
      double minExemplar,
      double maxExemplar)
      throws IOException {
    writer.write(name);
    writeAttributes(writer, attributes, additionalAttrKey, additionalAttrValue);
    writer.write(' ');
    writeDouble(writer, value);
    writer.write(' ');
    writeTimestamp(writer, epochNanos);
    writeExemplar(writer, exemplars, minExemplar, maxExemplar);
    writer.write('\n');
  }

  private static void writeAttributes(Writer writer, Attributes attributes) throws IOException {
    if (attributes.isEmpty()) {
      return;
    }
    writer.write('{');
    writeAttributePairs(writer, attributes);
    writer.write('}');
  }

  private static void writeAttributes(
      Writer writer, Attributes attributes, String additionalAttrKey, double additionalAttrValue)
      throws IOException {
    writer.write('{');
    writeAttributePairs(writer, attributes);
    writer.write(',');
    writer.write(additionalAttrKey);
    writer.write("=\"");
    writeDouble(writer, additionalAttrValue);
    writer.write('"');
    writer.write('}');
  }

  private static void writeAttributePairs(Writer writer, Attributes attributes) throws IOException {
    try {
      attributes.forEach(
          new BiConsumer<AttributeKey<?>, Object>() {
            private boolean wroteOne;

            @Override
            public void accept(AttributeKey<?> key, Object value) {
              try {
                if (wroteOne) {
                  writer.write(',');
                } else {
                  wroteOne = true;
                }
                writer.write(NameSanitizer.INSTANCE.apply(key.getKey()));
                writer.write("=\"");
                writeEscapedLabelValue(writer, value.toString());
                writer.write('"');
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            }
          });
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private static void writeDouble(Writer writer, double d) throws IOException {
    if (d == Double.POSITIVE_INFINITY) {
      writer.write("+Inf");
    } else if (d == Double.NEGATIVE_INFINITY) {
      writer.write("-Inf");
    } else {
      writer.write(Double.toString(d));
    }
  }

  static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
          writer.write("\\\\");
          break;
        case '\"':
          writer.write("\\\"");
          break;
        case '\n':
          writer.write("\\n");
          break;
        default:
          writer.write(c);
      }
    }
  }

  static class Prometheus004Serializer extends Serializer {

    Prometheus004Serializer(Predicate<String> metricNameFilter) {
      super(metricNameFilter);
    }

    @Override
    String contentType() {
      return "text/plain; version=0.0.4; charset=utf-8";
    }

    @Override
    String headerName(String name, PrometheusType type) {
      if (type == PrometheusType.COUNTER) {
        return name + "_total";
      }
      return name;
    }

    @Override
    void writeHelp(Writer writer, String help) throws IOException {
      for (int i = 0; i < help.length(); i++) {
        char c = help.charAt(i);
        switch (c) {
          case '\\':
            writer.write("\\\\");
            break;
          case '\n':
            writer.write("\\n");
            break;
          default:
            writer.write(c);
        }
      }
    }

    @Override
    void writeTimestamp(Writer writer, long timestampNanos) throws IOException {
      writer.write(Long.toString(TimeUnit.NANOSECONDS.toMillis(timestampNanos)));
    }

    @Override
    void writeExemplar(
        Writer writer, Collection<ExemplarData> exemplars, double minExemplar, double maxExemplar) {
      // Don't write exemplars
    }

    @Override
    void writeEof(Writer writer) {
      // Don't write EOF
    }
  }

  static class OpenMetrics100Serializer extends Serializer {

    OpenMetrics100Serializer(Predicate<String> metricNameFilter) {
      super(metricNameFilter);
    }

    @Override
    String contentType() {
      return "application/openmetrics-text; version=1.0.0; charset=utf-8";
    }

    @Override
    String headerName(String name, PrometheusType type) {
      return name;
    }

    @Override
    void writeHelp(Writer writer, String description) throws IOException {
      writeEscapedLabelValue(writer, description);
    }

    @Override
    void writeTimestamp(Writer writer, long timestampNanos) throws IOException {
      long timestampMillis = TimeUnit.NANOSECONDS.toMillis(timestampNanos);
      writer.write(Long.toString(timestampMillis / 1000));
      writer.write(".");
      long millis = timestampMillis % 1000;
      if (millis < 100) {
        writer.write('0');
      }
      if (millis < 10) {
        writer.write('0');
      }
      writer.write(Long.toString(millis));
    }

    @Override
    void writeExemplar(
        Writer writer, Collection<ExemplarData> exemplars, double minExemplar, double maxExemplar)
        throws IOException {
      for (ExemplarData exemplar : exemplars) {
        double value = getExemplarValue(exemplar);
        if (value > minExemplar && value <= maxExemplar) {
          writer.write(" # {");
          SpanContext spanContext = exemplar.getSpanContext();
          if (spanContext.isValid()) {
            // NB: Output sorted to match prometheus client library even though it shouldn't matter.
            // OTel generally outputs in trace_id span_id order though so we can consider breaking
            // from reference implementation if it makes sense.
            writer.write("span_id=\"");
            writer.write(spanContext.getSpanId());
            writer.write("\",trace_id=\"");
            writer.write(spanContext.getTraceId());
            writer.write('"');
          }
          writer.write("} ");
          writeDouble(writer, value);
          writer.write(' ');
          writeTimestamp(writer, exemplar.getEpochNanos());
          // Only write one exemplar.
          return;
        }
      }
    }

    @Override
    void writeEof(Writer writer) throws IOException {
      writer.write("# EOF\n");
    }
  }

  private static double getExemplarValue(ExemplarData exemplar) {
    return exemplar instanceof DoubleExemplarData
        ? ((DoubleExemplarData) exemplar).getValue()
        : (double) ((LongExemplarData) exemplar).getValue();
  }
}
