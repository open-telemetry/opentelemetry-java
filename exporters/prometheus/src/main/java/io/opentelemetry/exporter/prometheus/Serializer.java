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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import io.opentelemetry.sdk.resources.Resource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
      Writer writer,
      Collection<? extends ExemplarData> exemplars,
      double minExemplar,
      double maxExemplar)
      throws IOException;

  abstract void writeEof(Writer writer) throws IOException;

  final Set<String> write(Collection<MetricData> metrics, OutputStream output) throws IOException {
    Set<String> conflictMetricNames = new HashSet<>();
    Map<String, List<MetricData>> metricsByName = new LinkedHashMap<>();
    Set<InstrumentationScopeInfo> scopes = new LinkedHashSet<>();
    // Iterate through metrics, filtering and grouping by headerName
    for (MetricData metric : metrics) {
      // Not supported in specification yet.
      if (metric.getType() == MetricDataType.EXPONENTIAL_HISTOGRAM) {
        continue;
      }
      // PrometheusHttpServer#getAggregationTemporality specifies cumulative temporality for
      // all instruments, but non-SDK MetricProducers may not conform. We drop delta
      // temporality metrics to avoid the complexity of stateful transformation to cumulative.
      if (isDeltaTemporality(metric)) {
        continue;
      }
      PrometheusType prometheusType = PrometheusType.forMetric(metric);
      String metricName = metricName(metric.getName(), prometheusType);
      // Skip metrics which do not pass metricNameFilter
      if (!metricNameFilter.test(metricName)) {
        continue;
      }
      List<MetricData> metricsWithHeaderName =
          metricsByName.computeIfAbsent(metricName, unused -> new ArrayList<>());
      // Skip metrics with the same name but different type
      if (metricsWithHeaderName.size() > 0
          && prometheusType != PrometheusType.forMetric(metricsWithHeaderName.get(0))) {
        conflictMetricNames.add(metricName);
        continue;
      }

      metricsWithHeaderName.add(metric);
      scopes.add(metric.getInstrumentationScopeInfo());
    }

    Optional<Resource> optResource = metrics.stream().findFirst().map(MetricData::getResource);
    try (Writer writer =
        new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
      if (optResource.isPresent()) {
        writeResource(optResource.get(), writer);
      }
      for (InstrumentationScopeInfo scope : scopes) {
        writeScopeInfo(scope, writer);
      }
      for (Map.Entry<String, List<MetricData>> entry : metricsByName.entrySet()) {
        write(entry.getValue(), entry.getKey(), writer);
      }
      writeEof(writer);
    }
    return conflictMetricNames;
  }

  private void write(List<MetricData> metrics, String metricName, Writer writer)
      throws IOException {
    // Write header based on first metric
    MetricData first = metrics.get(0);
    PrometheusType type = PrometheusType.forMetric(first);
    String headerName = headerName(NameSanitizer.INSTANCE.apply(first.getName()), type);
    String description = metrics.get(0).getDescription();

    writer.write("# TYPE ");
    writer.write(headerName);
    writer.write(' ');
    writer.write(type.getTypeString());
    writer.write('\n');

    writer.write("# HELP ");
    writer.write(headerName);
    writer.write(' ');
    writeHelp(writer, description);
    writer.write('\n');

    // Then write the metrics.
    for (MetricData metric : metrics) {
      write(metric, metricName, writer);
    }
  }

  private void write(MetricData metric, String metricName, Writer writer) throws IOException {
    for (PointData point : getPoints(metric)) {
      switch (metric.getType()) {
        case DOUBLE_SUM:
        case DOUBLE_GAUGE:
          writePoint(
              writer,
              metric.getInstrumentationScopeInfo(),
              metricName,
              ((DoublePointData) point).getValue(),
              point.getAttributes(),
              point.getEpochNanos());
          break;
        case LONG_SUM:
        case LONG_GAUGE:
          writePoint(
              writer,
              metric.getInstrumentationScopeInfo(),
              metricName,
              (double) ((LongPointData) point).getValue(),
              point.getAttributes(),
              point.getEpochNanos());
          break;
        case HISTOGRAM:
          writeHistogram(
              writer, metric.getInstrumentationScopeInfo(), metricName, (HistogramPointData) point);
          break;
        case SUMMARY:
          writeSummary(
              writer, metric.getInstrumentationScopeInfo(), metricName, (SummaryPointData) point);
          break;
        case EXPONENTIAL_HISTOGRAM:
          throw new IllegalArgumentException("Can't happen");
      }
    }
  }

  private static boolean isDeltaTemporality(MetricData metricData) {
    switch (metricData.getType()) {
      case LONG_GAUGE:
      case DOUBLE_GAUGE:
      case SUMMARY:
        return false;
      case LONG_SUM:
        return metricData.getLongSumData().getAggregationTemporality()
            == AggregationTemporality.DELTA;
      case DOUBLE_SUM:
        return metricData.getDoubleSumData().getAggregationTemporality()
            == AggregationTemporality.DELTA;
      case HISTOGRAM:
        return metricData.getHistogramData().getAggregationTemporality()
            == AggregationTemporality.DELTA;
      default:
    }
    throw new IllegalArgumentException("Can't happen");
  }

  private static void writeResource(Resource resource, Writer writer) throws IOException {
    writer.write("# TYPE target info\n");
    writer.write("# HELP target Target metadata\n");
    writer.write("target_info{");
    writeAttributePairs(writer, /* initialComma= */ false, resource.getAttributes());
    writer.write("} 1\n");
  }

  private static void writeScopeInfo(
      InstrumentationScopeInfo instrumentationScopeInfo, Writer writer) throws IOException {
    writer.write("# TYPE otel_scope_info info\n");
    writer.write("# HELP otel_scope_info Scope metadata\n");
    writer.write("otel_scope_info{");
    writeScopeNameAndVersion(writer, instrumentationScopeInfo);
    writeAttributePairs(writer, /* initialComma= */ true, instrumentationScopeInfo.getAttributes());
    writer.write("} 1\n");
  }

  private void writeHistogram(
      Writer writer,
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      HistogramPointData point)
      throws IOException {
    writePoint(
        writer,
        instrumentationScopeInfo,
        name + "_count",
        (double) point.getCount(),
        point.getAttributes(),
        point.getEpochNanos());
    writePoint(
        writer,
        instrumentationScopeInfo,
        name + "_sum",
        point.getSum(),
        point.getAttributes(),
        point.getEpochNanos());

    long cumulativeCount = 0;
    List<Long> counts = point.getCounts();
    for (int i = 0; i < counts.size(); i++) {
      // This is the upper boundary (inclusive). I.e. all values should be < this value (LE -
      // Less-then-or-Equal).
      double boundary = getBucketUpperBound(point, i);

      cumulativeCount += counts.get(i);
      writePoint(
          writer,
          instrumentationScopeInfo,
          name + "_bucket",
          (double) cumulativeCount,
          point.getAttributes(),
          point.getEpochNanos(),
          "le",
          boundary,
          point.getExemplars(),
          getBucketLowerBound(point, i),
          boundary);
    }
  }

  /**
   * Returns the lower bound of a bucket (all values would have been greater than).
   *
   * @param bucketIndex The bucket index, should match {@link HistogramPointData#getCounts()} index.
   */
  static double getBucketLowerBound(HistogramPointData point, int bucketIndex) {
    return bucketIndex > 0 ? point.getBoundaries().get(bucketIndex - 1) : Double.NEGATIVE_INFINITY;
  }

  /**
   * Returns the upper inclusive bound of a bucket (all values would have been less then or equal).
   *
   * @param bucketIndex The bucket index, should match {@link HistogramPointData#getCounts()} index.
   */
  static double getBucketUpperBound(HistogramPointData point, int bucketIndex) {
    List<Double> boundaries = point.getBoundaries();
    return (bucketIndex < boundaries.size())
        ? boundaries.get(bucketIndex)
        : Double.POSITIVE_INFINITY;
  }

  private void writeSummary(
      Writer writer,
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      SummaryPointData point)
      throws IOException {
    writePoint(
        writer,
        instrumentationScopeInfo,
        name + "_count",
        (double) point.getCount(),
        point.getAttributes(),
        point.getEpochNanos());
    writePoint(
        writer,
        instrumentationScopeInfo,
        name + "_sum",
        point.getSum(),
        point.getAttributes(),
        point.getEpochNanos());

    List<ValueAtQuantile> valueAtQuantiles = point.getValues();
    for (ValueAtQuantile valueAtQuantile : valueAtQuantiles) {
      writePoint(
          writer,
          instrumentationScopeInfo,
          name,
          valueAtQuantile.getValue(),
          point.getAttributes(),
          point.getEpochNanos(),
          "quantile",
          valueAtQuantile.getQuantile(),
          Collections.emptyList(),
          0,
          0);
    }
  }

  private void writePoint(
      Writer writer,
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      double value,
      Attributes attributes,
      long epochNanos)
      throws IOException {
    writer.write(name);
    writeAttributes(writer, instrumentationScopeInfo, attributes);
    writer.write(' ');
    writeDouble(writer, value);
    writer.write(' ');
    writeTimestamp(writer, epochNanos);
    writer.write('\n');
  }

  private void writePoint(
      Writer writer,
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      double value,
      Attributes attributes,
      long epochNanos,
      String additionalAttrKey,
      double additionalAttrValue,
      Collection<? extends ExemplarData> exemplars,
      double minExemplar,
      double maxExemplar)
      throws IOException {
    writer.write(name);
    writeAttributes(
        writer, instrumentationScopeInfo, attributes, additionalAttrKey, additionalAttrValue);
    writer.write(' ');
    writeDouble(writer, value);
    writer.write(' ');
    writeTimestamp(writer, epochNanos);
    writeExemplar(writer, exemplars, minExemplar, maxExemplar);
    writer.write('\n');
  }

  private static void writeAttributes(
      Writer writer, InstrumentationScopeInfo instrumentationScopeInfo, Attributes attributes)
      throws IOException {
    writer.write('{');
    writeScopeNameAndVersion(writer, instrumentationScopeInfo);
    if (!attributes.isEmpty()) {
      writeAttributePairs(writer, /* initialComma= */ true, attributes);
    }
    writer.write('}');
  }

  private static void writeAttributes(
      Writer writer,
      InstrumentationScopeInfo instrumentationScopeInfo,
      Attributes attributes,
      String additionalAttrKey,
      double additionalAttrValue)
      throws IOException {
    writer.write('{');
    writeScopeNameAndVersion(writer, instrumentationScopeInfo);
    writer.write(',');
    if (!attributes.isEmpty()) {
      writeAttributePairs(writer, /* initialComma= */ false, attributes);
      writer.write(',');
    }
    writer.write(additionalAttrKey);
    writer.write("=\"");
    writeDouble(writer, additionalAttrValue);
    writer.write('"');
    writer.write('}');
  }

  private static void writeScopeNameAndVersion(
      Writer writer, InstrumentationScopeInfo instrumentationScopeInfo) throws IOException {
    writer.write("otel_scope_name=\"");
    writer.write(instrumentationScopeInfo.getName());
    writer.write("\"");
    if (instrumentationScopeInfo.getVersion() != null) {
      writer.write(",otel_scope_version=\"");
      writer.write(instrumentationScopeInfo.getVersion());
      writer.write("\"");
    }
  }

  private static void writeAttributePairs(
      Writer writer, boolean initialComma, Attributes attributes) throws IOException {
    try {
      attributes.forEach(
          new BiConsumer<AttributeKey<?>, Object>() {
            private boolean prefixWithComma = initialComma;

            @Override
            public void accept(AttributeKey<?> key, Object value) {
              try {
                if (prefixWithComma) {
                  writer.write(',');
                } else {
                  prefixWithComma = true;
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
        Writer writer,
        Collection<? extends ExemplarData> exemplars,
        double minExemplar,
        double maxExemplar) {
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
        Writer writer,
        Collection<? extends ExemplarData> exemplars,
        double minExemplar,
        double maxExemplar)
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

  static Collection<? extends PointData> getPoints(MetricData metricData) {
    switch (metricData.getType()) {
      case DOUBLE_GAUGE:
        return metricData.getDoubleGaugeData().getPoints();
      case DOUBLE_SUM:
        return metricData.getDoubleSumData().getPoints();
      case LONG_GAUGE:
        return metricData.getLongGaugeData().getPoints();
      case LONG_SUM:
        return metricData.getLongSumData().getPoints();
      case SUMMARY:
        return metricData.getSummaryData().getPoints();
      case HISTOGRAM:
        return metricData.getHistogramData().getPoints();
      case EXPONENTIAL_HISTOGRAM:
        return metricData.getExponentialHistogramData().getPoints();
    }
    return Collections.emptyList();
  }

  private static String metricName(String rawMetricName, PrometheusType type) {
    String name = NameSanitizer.INSTANCE.apply(rawMetricName);
    if (type == PrometheusType.COUNTER) {
      name = name + "_total";
    }
    return name;
  }

  private static double getExemplarValue(ExemplarData exemplar) {
    return exemplar instanceof DoubleExemplarData
        ? ((DoubleExemplarData) exemplar).getValue()
        : (double) ((LongExemplarData) exemplar).getValue();
  }
}
