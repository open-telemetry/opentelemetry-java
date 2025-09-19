/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"resource", "ConcatenationWithEmptyString"})
class PrometheusMetricReaderTest {

  private final TestClock testClock = TestClock.create();
  private String createdTimestamp;
  private PrometheusMetricReader reader;
  private Meter meter;
  private Tracer tracer;

  @SuppressWarnings("resource")
  @BeforeEach
  void setUp() {
    this.testClock.setTime(Instant.ofEpochMilli((System.currentTimeMillis() / 100) * 100));
    this.createdTimestamp = convertTimestamp(testClock.now());
    this.reader = new PrometheusMetricReader(/* allowedResourceAttributesFilter= */ null);
    this.meter =
        SdkMeterProvider.builder()
            .setClock(testClock)
            .registerMetricReader(this.reader)
            .setResource(
                Resource.getDefault().toBuilder().put("telemetry.sdk.version", "1.x.x").build())
            .registerView(
                InstrumentSelector.builder().setName("my.exponential.histogram").build(),
                View.builder()
                    .setAggregation(Aggregation.base2ExponentialBucketHistogram())
                    .build())
            .build()
            .meterBuilder("test")
            .build();
    this.tracer =
        SdkTracerProvider.builder().setClock(testClock).build().tracerBuilder("test").build();
  }

  @Test
  void longCounterComplete() throws IOException {
    LongCounter counter =
        meter
            .counterBuilder("requests.size")
            .setDescription("some help text")
            .setUnit("By")
            .build();
    Span span1 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span1.makeCurrent()) {
      counter.add(3, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    Span span2 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span2.makeCurrent()) {
      counter.add(2, Attributes.builder().put("animal", "mouse").build());
    } finally {
      span2.end();
    }
    assertCounterComplete(reader.collect(), span1, span2);
  }

  @Test
  void doubleCounterComplete() throws IOException {
    DoubleCounter counter =
        meter
            .counterBuilder("requests.size")
            .setDescription("some help text")
            .setUnit("By")
            .ofDoubles()
            .build();
    Span span1 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span1.makeCurrent()) {
      counter.add(3.0, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    Span span2 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span2.makeCurrent()) {
      counter.add(2.0, Attributes.builder().put("animal", "mouse").build());
    } finally {
      span2.end();
    }
    assertCounterComplete(reader.collect(), span1, span2);
  }

  private void assertCounterComplete(MetricSnapshots snapshots, Span span1, Span span2)
      throws IOException {
    String expected =
        ""
            + "# TYPE requests_size_bytes counter\n"
            + "# UNIT requests_size_bytes bytes\n"
            + "# HELP requests_size_bytes some help text\n"
            + "requests_size_bytes_total{animal=\"bear\",otel_scope_name=\"test\"} 3.0 # {span_id=\""
            + span1.getSpanContext().getSpanId()
            + "\",trace_id=\""
            + span1.getSpanContext().getTraceId()
            + "\"} 3.0 <timestamp>\n"
            + "requests_size_bytes_created{animal=\"bear\",otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "requests_size_bytes_total{animal=\"mouse\",otel_scope_name=\"test\"} 2.0 # {span_id=\""
            + span2.getSpanContext().getSpanId()
            + "\",trace_id=\""
            + span2.getSpanContext().getTraceId()
            + "\"} 2.0 <timestamp>\n"
            + "requests_size_bytes_created{animal=\"mouse\",otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertMatches(expected, toOpenMetrics(snapshots));
  }

  @Test
  void longCounterMinimal() throws IOException {
    LongCounter counter = meter.counterBuilder("requests").build();
    counter.add(2);
    assertCounterMinimal(reader.collect());
  }

  @Test
  void doubleCounterMinimal() throws IOException {
    DoubleCounter counter = meter.counterBuilder("requests").ofDoubles().build();
    counter.add(2.0);
    assertCounterMinimal(reader.collect());
  }

  private void assertCounterMinimal(MetricSnapshots snapshots) throws IOException {
    String expected =
        ""
            + "# TYPE requests counter\n"
            + "requests_total{otel_scope_name=\"test\"} 2.0\n"
            + "requests_created{otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(snapshots)).isEqualTo(expected);
  }

  @Test
  void longUpDownCounterComplete() throws IOException {
    LongUpDownCounter counter =
        meter
            .upDownCounterBuilder("queue.size")
            .setDescription("some help text")
            .setUnit("By")
            .build();
    Span span1 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span1.makeCurrent()) {
      counter.add(3, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    Span span2 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span2.makeCurrent()) {
      counter.add(2, Attributes.builder().put("animal", "mouse").build());
    } finally {
      span2.end();
    }
    assertUpDownCounterComplete(reader.collect(), span1, span2);
  }

  @Test
  void doubleUpDownCounterComplete() throws IOException {
    DoubleUpDownCounter counter =
        meter
            .upDownCounterBuilder("queue.size")
            .setDescription("some help text")
            .setUnit("By")
            .ofDoubles()
            .build();
    Span span1 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span1.makeCurrent()) {
      counter.add(3.0, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    Span span2 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span2.makeCurrent()) {
      counter.add(2.0, Attributes.builder().put("animal", "mouse").build());
    } finally {
      span2.end();
    }
    assertUpDownCounterComplete(reader.collect(), span1, span2);
  }

  private static void assertUpDownCounterComplete(MetricSnapshots snapshots, Span span1, Span span2)
      throws IOException {
    String expected =
        ""
            + "# TYPE queue_size_bytes gauge\n"
            + "# UNIT queue_size_bytes bytes\n"
            + "# HELP queue_size_bytes some help text\n"
            + "queue_size_bytes{animal=\"bear\",otel_scope_name=\"test\"} 3.0 # {span_id=\""
            + span1.getSpanContext().getSpanId()
            + "\",trace_id=\""
            + span1.getSpanContext().getTraceId()
            + "\"} 3.0 <timestamp>\n"
            + "queue_size_bytes{animal=\"mouse\",otel_scope_name=\"test\"} 2.0 # {span_id=\""
            + span2.getSpanContext().getSpanId()
            + "\",trace_id=\""
            + span2.getSpanContext().getTraceId()
            + "\"} 2.0 <timestamp>\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertMatches(expected, toOpenMetrics(snapshots));
  }

  @Test
  void longUpDownCounterMinimal() throws IOException {
    LongUpDownCounter counter = meter.upDownCounterBuilder("users.active").build();
    counter.add(27);
    assertUpDownCounterMinimal(reader.collect());
  }

  @Test
  void doubleUpDownCounterMinimal() throws IOException {
    DoubleUpDownCounter counter = meter.upDownCounterBuilder("users.active").ofDoubles().build();
    counter.add(27.0);
    assertUpDownCounterMinimal(reader.collect());
  }

  private static void assertUpDownCounterMinimal(MetricSnapshots snapshots) throws IOException {
    String expected =
        ""
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# TYPE users_active gauge\n"
            + "users_active{otel_scope_name=\"test\"} 27.0\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(snapshots)).isEqualTo(expected);
  }

  @Test
  void longGaugeComplete() throws IOException {
    meter
        .gaugeBuilder("temperature")
        .setUnit("Cel")
        .setDescription("help text")
        .ofLongs()
        .buildWithCallback(
            m -> {
              m.record(23, Attributes.builder().put("location", "inside").build());
              m.record(17, Attributes.builder().put("location", "outside").build());
            });
    assertGaugeComplete(reader.collect());
  }

  @Test
  void doubleGaugeComplete() throws IOException {
    meter
        .gaugeBuilder("temperature")
        .setUnit("Cel")
        .setDescription("help text")
        .buildWithCallback(
            m -> {
              m.record(23.0, Attributes.builder().put("location", "inside").build());
              m.record(17.0, Attributes.builder().put("location", "outside").build());
            });
    assertGaugeComplete(reader.collect());
  }

  private static void assertGaugeComplete(MetricSnapshots snapshots) throws IOException {
    String expected =
        ""
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# TYPE temperature_celsius gauge\n"
            + "# UNIT temperature_celsius celsius\n"
            + "# HELP temperature_celsius help text\n"
            + "temperature_celsius{location=\"inside\",otel_scope_name=\"test\"} 23.0\n"
            + "temperature_celsius{location=\"outside\",otel_scope_name=\"test\"} 17.0\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(snapshots)).isEqualTo(expected);
  }

  @Test
  void longGaugeMinimal() throws IOException {
    meter.gaugeBuilder("my_gauge").ofLongs().buildWithCallback(m -> m.record(2));
    assertGaugeMinimal(reader.collect());
  }

  @Test
  void doubleGaugeMinimal() throws IOException {
    meter.gaugeBuilder("my_gauge").buildWithCallback(m -> m.record(2.0));
    assertGaugeMinimal(reader.collect());
  }

  private static void assertGaugeMinimal(MetricSnapshots snapshots) throws IOException {
    String expected =
        ""
            + "# TYPE my_gauge gauge\n"
            + "my_gauge{otel_scope_name=\"test\"} 2.0\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(snapshots)).isEqualTo(expected);
  }

  @Test
  void longHistogramComplete() throws IOException {
    LongHistogram histogram =
        meter
            .histogramBuilder("request.size")
            .setDescription("some help text")
            .setUnit("By")
            .ofLongs()
            .build();
    Span span1 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span1.makeCurrent()) {
      histogram.record(173, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    Span span2 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span2.makeCurrent()) {
      histogram.record(400, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    Span span3 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span3.makeCurrent()) {
      histogram.record(204, Attributes.builder().put("animal", "mouse").build());
    } finally {
      span3.end();
    }
    assertHistogramComplete(reader.collect(), span1, span2, span3);
  }

  @Test
  void doubleHistogramComplete() throws IOException {
    DoubleHistogram histogram =
        meter
            .histogramBuilder("request.size")
            .setDescription("some help text")
            .setUnit("By")
            .build();
    Span span1 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span1.makeCurrent()) {
      histogram.record(173.0, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    Span span2 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span2.makeCurrent()) {
      histogram.record(400.0, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    Span span3 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span3.makeCurrent()) {
      histogram.record(204.0, Attributes.builder().put("animal", "mouse").build());
    } finally {
      span3.end();
    }
    assertHistogramComplete(reader.collect(), span1, span2, span3);
  }

  private void assertHistogramComplete(
      MetricSnapshots snapshots, Span span1, Span span2, Span span3) throws IOException {
    String expected =
        ""
            + "# TYPE request_size_bytes histogram\n"
            + "# UNIT request_size_bytes bytes\n"
            + "# HELP request_size_bytes some help text\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"0.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"5.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"10.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"25.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"50.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"75.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"100.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"250.0\"} 1 # {span_id=\""
            + span1.getSpanContext().getSpanId()
            + "\",trace_id=\""
            + span1.getSpanContext().getTraceId()
            + "\"} 173.0 <timestamp>\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"500.0\"} 2 # {span_id=\""
            + span2.getSpanContext().getSpanId()
            + "\",trace_id=\""
            + span2.getSpanContext().getTraceId()
            + "\"} 400.0 <timestamp>\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"750.0\"} 2\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"1000.0\"} 2\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"2500.0\"} 2\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"5000.0\"} 2\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"7500.0\"} 2\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"10000.0\"} 2\n"
            + "request_size_bytes_bucket{animal=\"bear\",otel_scope_name=\"test\",le=\"+Inf\"} 2\n"
            + "request_size_bytes_count{animal=\"bear\",otel_scope_name=\"test\"} 2 # {span_id=\""
            + "<spanId>"
            + "\",trace_id=\""
            + "<traceId>"
            + "\"} <measurement> <timestamp>\n"
            + "request_size_bytes_sum{animal=\"bear\",otel_scope_name=\"test\"} 573.0\n"
            + "request_size_bytes_created{animal=\"bear\",otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"0.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"5.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"10.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"25.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"50.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"75.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"100.0\"} 0\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"250.0\"} 1 # {span_id=\""
            + span3.getSpanContext().getSpanId()
            + "\",trace_id=\""
            + span3.getSpanContext().getTraceId()
            + "\"} 204.0 <timestamp>\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"500.0\"} 1\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"750.0\"} 1\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"1000.0\"} 1\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"2500.0\"} 1\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"5000.0\"} 1\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"7500.0\"} 1\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"10000.0\"} 1\n"
            + "request_size_bytes_bucket{animal=\"mouse\",otel_scope_name=\"test\",le=\"+Inf\"} 1\n"
            + "request_size_bytes_count{animal=\"mouse\",otel_scope_name=\"test\"} 1 # {span_id=\""
            + span3.getSpanContext().getSpanId()
            + "\",trace_id=\""
            + span3.getSpanContext().getTraceId()
            + "\"} 204.0 <timestamp>\n"
            + "request_size_bytes_sum{animal=\"mouse\",otel_scope_name=\"test\"} 204.0\n"
            + "request_size_bytes_created{animal=\"mouse\",otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertMatches(expected, toOpenMetrics(snapshots));
  }

  @Test
  void longHistogramMinimal() throws IOException {
    LongHistogram histogram = meter.histogramBuilder("request.size").ofLongs().build();
    histogram.record(173);
    histogram.record(173);
    histogram.record(100_000);
    assertHistogramMinimal(reader.collect());
  }

  @Test
  void doubleHistogramMinimal() throws IOException {
    DoubleHistogram histogram = meter.histogramBuilder("request.size").build();
    histogram.record(173.0);
    histogram.record(173.0);
    histogram.record(100_000.0);
    assertHistogramMinimal(reader.collect());
  }

  private void assertHistogramMinimal(MetricSnapshots snapshots) throws IOException {
    String expected =
        ""
            + "# TYPE request_size histogram\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"0.0\"} 0\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"5.0\"} 0\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"10.0\"} 0\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"25.0\"} 0\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"50.0\"} 0\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"75.0\"} 0\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"100.0\"} 0\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"250.0\"} 2\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"500.0\"} 2\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"750.0\"} 2\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"1000.0\"} 2\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"2500.0\"} 2\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"5000.0\"} 2\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"7500.0\"} 2\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"10000.0\"} 2\n"
            + "request_size_bucket{otel_scope_name=\"test\",le=\"+Inf\"} 3\n"
            + "request_size_count{otel_scope_name=\"test\"} 3\n"
            + "request_size_sum{otel_scope_name=\"test\"} 100346.0\n"
            + "request_size_created{otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(snapshots)).isEqualTo(expected);
  }

  @Test
  @Disabled("disabled until #6010 is fixed")
  void exponentialLongHistogramComplete() {
    LongHistogram histogram =
        meter
            .histogramBuilder("my.exponential.histogram")
            .setDescription("some help text")
            .setUnit("By")
            .ofLongs()
            .build();
    Span span1 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span1.makeCurrent()) {
      histogram.record(7, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    histogram.record(0, Attributes.builder().put("animal", "bear").build());
    Span span2 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span2.makeCurrent()) {
      histogram.record(3, Attributes.builder().put("animal", "mouse").build());
    } finally {
      span2.end();
    }
    assertExponentialHistogramComplete(reader.collect(), span1, span2);
  }

  @Test
  void exponentialDoubleHistogramComplete() {
    DoubleHistogram histogram =
        meter
            .histogramBuilder("my.exponential.histogram")
            .setDescription("some help text")
            .setUnit("By")
            .build();
    Span span1 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span1.makeCurrent()) {
      histogram.record(7.0, Attributes.builder().put("animal", "bear").build());
    } finally {
      span1.end();
    }
    histogram.record(0.0, Attributes.builder().put("animal", "bear").build());
    Span span2 = tracer.spanBuilder("test").startSpan();
    try (Scope ignored = span2.makeCurrent()) {
      histogram.record(3.0, Attributes.builder().put("animal", "mouse").build());
    } finally {
      span2.end();
    }
    assertExponentialHistogramComplete(reader.collect(), span1, span2);
  }

  private static void assertExponentialHistogramComplete(
      MetricSnapshots snapshots, Span span1, Span span2) {
    String expected =
        ""
            + "name: \"my_exponential_histogram_bytes\"\n"
            + "help: \"some help text\"\n"
            + "type: HISTOGRAM\n"
            + "metric {\n"
            + "  label {\n"
            + "    name: \"animal\"\n"
            + "    value: \"bear\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"otel_scope_name\"\n"
            + "    value: \"test\"\n"
            + "  }\n"
            + "  histogram {\n"
            + "    sample_count: 2\n"
            + "    sample_sum: 7.0\n"
            + "    bucket {\n"
            + "      cumulative_count: 2\n"
            + "      upper_bound: Infinity\n"
            + "      exemplar {\n"
            + "        label {\n"
            + "          name: \"span_id\"\n"
            + "          value: \""
            + span1.getSpanContext().getSpanId()
            + "\"\n"
            + "        }\n"
            + "        label {\n"
            + "          name: \"trace_id\"\n"
            + "          value: \""
            + span1.getSpanContext().getTraceId()
            + "\"\n"
            + "        }\n"
            + "        value: 7.0\n"
            + "        timestamp {\n"
            + "          seconds: <timestamp>\n"
            + "          <maybeNanos>\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "    schema: 8\n"
            + "    zero_threshold: 0.0\n"
            + "    zero_count: 1\n"
            + "    positive_span {\n"
            + "      offset: 719\n"
            + "      length: 1\n"
            + "    }\n"
            + "    positive_delta: 1\n"
            + "  }\n"
            + "}\n"
            + "metric {\n"
            + "  label {\n"
            + "    name: \"animal\"\n"
            + "    value: \"mouse\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"otel_scope_name\"\n"
            + "    value: \"test\"\n"
            + "  }\n"
            + "  histogram {\n"
            + "    sample_count: 1\n"
            + "    sample_sum: 3.0\n"
            + "    bucket {\n"
            + "      cumulative_count: 1\n"
            + "      upper_bound: Infinity\n"
            + "      exemplar {\n"
            + "        label {\n"
            + "          name: \"span_id\"\n"
            + "          value: \""
            + span2.getSpanContext().getSpanId()
            + "\"\n"
            + "        }\n"
            + "        label {\n"
            + "          name: \"trace_id\"\n"
            + "          value: \""
            + span2.getSpanContext().getTraceId()
            + "\"\n"
            + "        }\n"
            + "        value: 3.0\n"
            + "        timestamp {\n"
            + "          seconds: <timestamp>\n"
            + "          <maybeNanos>\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "    schema: 8\n"
            + "    zero_threshold: 0.0\n"
            + "    zero_count: 0\n"
            + "    positive_span {\n"
            + "      offset: 406\n"
            + "      length: 1\n"
            + "    }\n"
            + "    positive_delta: 1\n"
            + "  }\n"
            + "}\n"
            + "name: \"target_info\"\n"
            + "type: GAUGE\n"
            + "metric {\n"
            + "  label {\n"
            + "    name: \"service_name\"\n"
            + "    value: \"unknown_service:java\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"telemetry_sdk_language\"\n"
            + "    value: \"java\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"telemetry_sdk_name\"\n"
            + "    value: \"opentelemetry\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"telemetry_sdk_version\"\n"
            + "    value: \"1.x.x\"\n"
            + "  }\n"
            + "  gauge {\n"
            + "    value: 1.0\n"
            + "  }\n"
            + "}\n";
    assertMatches(expected, toPrometheusProtobuf(snapshots));
  }

  @Test
  void exponentialLongHistogramMinimal() {
    LongHistogram histogram = meter.histogramBuilder("my.exponential.histogram").ofLongs().build();
    histogram.record(1, Attributes.builder().put("animal", "bear").build());
    assertExponentialHistogramMinimal(reader.collect());
  }

  @Test
  void exponentialDoubleHistogramMinimal() {
    DoubleHistogram histogram = meter.histogramBuilder("my.exponential.histogram").build();
    histogram.record(1.0, Attributes.builder().put("animal", "bear").build());
    assertExponentialHistogramMinimal(reader.collect());
  }

  private static void assertExponentialHistogramMinimal(MetricSnapshots snapshots) {
    String expected =
        ""
            + "name: \"my_exponential_histogram\"\n"
            + "help: \"\"\n"
            + "type: HISTOGRAM\n"
            + "metric {\n"
            + "  label {\n"
            + "    name: \"animal\"\n"
            + "    value: \"bear\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"otel_scope_name\"\n"
            + "    value: \"test\"\n"
            + "  }\n"
            + "  histogram {\n"
            + "    sample_count: 1\n"
            + "    sample_sum: 1.0\n"
            + "    schema: 8\n"
            + "    zero_threshold: 0.0\n"
            + "    zero_count: 0\n"
            + "    positive_span {\n"
            + "      offset: 0\n"
            + "      length: 1\n"
            + "    }\n"
            + "    positive_delta: 1\n"
            + "  }\n"
            + "}\n"
            + "name: \"target_info\"\n"
            + "type: GAUGE\n"
            + "metric {\n"
            + "  label {\n"
            + "    name: \"service_name\"\n"
            + "    value: \"unknown_service:java\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"telemetry_sdk_language\"\n"
            + "    value: \"java\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"telemetry_sdk_name\"\n"
            + "    value: \"opentelemetry\"\n"
            + "  }\n"
            + "  label {\n"
            + "    name: \"telemetry_sdk_version\"\n"
            + "    value: \"1.x.x\"\n"
            + "  }\n"
            + "  gauge {\n"
            + "    value: 1.0\n"
            + "  }\n"
            + "}\n";
    assertMatches(expected, toPrometheusProtobuf(snapshots));
  }

  @Test
  void exponentialHistogramBucketConversion() {
    Random random = new Random();
    for (int i = 0; i < 100_000; i++) {
      int otelScale = random.nextInt(24) - 4;
      int prometheusScale = Math.min(otelScale, 8);
      PrometheusMetricReader reader =
          new PrometheusMetricReader(/* allowedResourceAttributesFilter= */ null);
      Meter meter =
          SdkMeterProvider.builder()
              .registerMetricReader(reader)
              .registerView(
                  InstrumentSelector.builder().setName("my.exponential.histogram").build(),
                  View.builder()
                      .setAggregation(Aggregation.base2ExponentialBucketHistogram(160, otelScale))
                      .build())
              .build()
              .meterBuilder("test")
              .build();
      int orderOfMagnitude = random.nextInt(18) - 9;
      double observation = random.nextDouble() * Math.pow(10, orderOfMagnitude);
      if (observation == 0) {
        continue;
      }
      DoubleHistogram histogram = meter.histogramBuilder("my.exponential.histogram").build();
      histogram.record(observation);
      MetricSnapshots snapshots = reader.collect();
      HistogramSnapshot snapshot = (HistogramSnapshot) snapshots.get(0);
      HistogramDataPointSnapshot dataPoint = snapshot.getDataPoints().get(0);
      assertThat(dataPoint.getNativeSchema()).isEqualTo(prometheusScale);
      NativeHistogramBuckets buckets = dataPoint.getNativeBucketsForPositiveValues();
      assertThat(buckets.size()).isEqualTo(1);
      int index = buckets.getBucketIndex(0);
      double base = Math.pow(2, Math.pow(2, -prometheusScale));
      double lowerBound = Math.pow(base, index - 1);
      double upperBound = Math.pow(base, index);
      assertThat(lowerBound).isLessThan(observation);
      assertThat(upperBound).isGreaterThanOrEqualTo(observation);
    }
  }

  @Test
  void exponentialLongHistogramScaleDown() {
    // The following histogram will have the default scale, which is 20.
    DoubleHistogram histogram = meter.histogramBuilder("my.exponential.histogram").build();
    double base = Math.pow(2, Math.pow(2, -20));
    int i;
    for (i = 0; i < Math.pow(2, 12); i++) {
      histogram.record(Math.pow(base, i)); // one observation per bucket
    }
    for (int j = 0; j < 10; j++) {
      histogram.record(Math.pow(base, i + 2 * j)); // few empty buckets between the observations
    }
    MetricSnapshots snapshots = reader.collect();
    HistogramSnapshot snapshot = (HistogramSnapshot) snapshots.get(0);
    HistogramDataPointSnapshot dataPoint = snapshot.getDataPoints().get(0);
    assertThat(dataPoint.getNativeSchema()).isEqualTo(8); // scaled down from 20 to 8.
    NativeHistogramBuckets buckets = dataPoint.getNativeBucketsForPositiveValues();
    assertThat(buckets.size()).isEqualTo(3);
    // In bucket 0 we have exactly one observation: the value 1.0
    assertThat(buckets.getBucketIndex(0)).isEqualTo(0);
    assertThat(buckets.getCount(0)).isEqualTo(1);
    // In bucket 1 we have 4095 observations
    assertThat(buckets.getBucketIndex(1)).isEqualTo(1);
    assertThat(buckets.getCount(1)).isEqualTo(4095);
    // In bucket 2 we have 10 observations (despite the empty buckets all observations fall into the
    // same bucket at scale 8)
    assertThat(buckets.getBucketIndex(2)).isEqualTo(2);
    assertThat(buckets.getCount(2)).isEqualTo(10);
  }

  @Test
  void instrumentationScope() throws IOException {
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .setClock(testClock)
            .registerMetricReader(this.reader)
            .setResource(
                Resource.getDefault().toBuilder().put("telemetry.sdk.version", "1.x.x").build())
            .build();
    Meter meter1 = meterProvider.meterBuilder("scopeA").setInstrumentationVersion("1.1").build();
    Meter meter2 = meterProvider.meterBuilder("scopeB").setInstrumentationVersion("1.2").build();
    meter1
        .counterBuilder("processing.time")
        .setDescription("processing time in seconds")
        .setUnit("s")
        .ofDoubles()
        .build()
        .add(3.3, Attributes.builder().put("a", "b").build());
    meter2
        .counterBuilder("processing.time")
        .setDescription("processing time in seconds")
        .setUnit("s")
        .ofDoubles()
        .build()
        .add(3.3, Attributes.builder().put("a", "b").build());
    String expected =
        ""
            + "# TYPE processing_time_seconds counter\n"
            + "# UNIT processing_time_seconds seconds\n"
            + "# HELP processing_time_seconds processing time in seconds\n"
            + "processing_time_seconds_total{a=\"b\",otel_scope_name=\"scopeA\",otel_scope_version=\"1.1\"} 3.3\n"
            + "processing_time_seconds_created{a=\"b\",otel_scope_name=\"scopeA\",otel_scope_version=\"1.1\"} "
            + createdTimestamp
            + "\n"
            + "processing_time_seconds_total{a=\"b\",otel_scope_name=\"scopeB\",otel_scope_version=\"1.2\"} 3.3\n"
            + "processing_time_seconds_created{a=\"b\",otel_scope_name=\"scopeB\",otel_scope_version=\"1.2\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(reader.collect())).isEqualTo(expected);
  }

  @Test
  void nameSuffix() throws IOException {
    LongCounter unitAndTotal =
        meter.counterBuilder("request.duration.seconds.total").setUnit("s").build();
    unitAndTotal.add(1);
    LongCounter unitOnly = meter.counterBuilder("response.duration.seconds").setUnit("s").build();
    unitOnly.add(2);
    LongCounter totalOnly = meter.counterBuilder("processing.duration.total").setUnit("s").build();
    totalOnly.add(3);
    LongCounter noSuffix = meter.counterBuilder("queue.time").setUnit("s").build();
    noSuffix.add(4);
    String expected =
        ""
            + "# TYPE processing_duration_seconds counter\n"
            + "# UNIT processing_duration_seconds seconds\n"
            + "processing_duration_seconds_total{otel_scope_name=\"test\"} 3.0\n"
            + "processing_duration_seconds_created{otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE queue_time_seconds counter\n"
            + "# UNIT queue_time_seconds seconds\n"
            + "queue_time_seconds_total{otel_scope_name=\"test\"} 4.0\n"
            + "queue_time_seconds_created{otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE request_duration_seconds counter\n"
            + "# UNIT request_duration_seconds seconds\n"
            + "request_duration_seconds_total{otel_scope_name=\"test\"} 1.0\n"
            + "request_duration_seconds_created{otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE response_duration_seconds counter\n"
            + "# UNIT response_duration_seconds seconds\n"
            + "response_duration_seconds_total{otel_scope_name=\"test\"} 2.0\n"
            + "response_duration_seconds_created{otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(reader.collect())).isEqualTo(expected);
  }

  @Test
  void nameSuffixUnit() throws IOException {
    LongCounter counter = meter.counterBuilder("request.duration.seconds").setUnit("s").build();
    counter.add(1);
    String expected =
        ""
            + "# TYPE request_duration_seconds counter\n"
            + "# UNIT request_duration_seconds seconds\n"
            + "request_duration_seconds_total{otel_scope_name=\"test\"} 1.0\n"
            + "request_duration_seconds_created{otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(reader.collect())).isEqualTo(expected);
  }

  @Test
  void illegalCharacters() throws IOException {
    LongCounter counter = meter.counterBuilder("prod/request.count").build();
    counter.add(1, Attributes.builder().put("user-count", 30).build());
    String expected =
        ""
            + "# TYPE prod_request_count counter\n"
            + "prod_request_count_total{otel_scope_name=\"test\",user_count=\"30\"} 1.0\n"
            + "prod_request_count_created{otel_scope_name=\"test\",user_count=\"30\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(reader.collect())).isEqualTo(expected);
  }

  @Test
  void createdTimestamp() throws IOException {

    LongCounter counter = meter.counterBuilder("requests").build();
    testClock.advance(Duration.ofMillis(1));
    counter.add(3, Attributes.builder().put("animal", "bear").build());
    testClock.advance(Duration.ofMillis(1));
    counter.add(2, Attributes.builder().put("animal", "mouse").build());
    testClock.advance(Duration.ofMillis(1));

    // There is a curious difference between Prometheus and OpenTelemetry:
    // In Prometheus metrics the _created timestamp is per data point,
    // i.e. the _created timestamp says when this specific set of label values
    // was first observed.
    // In the OTel Java SDK the _created timestamp is the initialization time
    // of the SdkMeterProvider, i.e. all data points will have the same _created timestamp.
    // So we expect the _created timestamp to be the start time of the application,
    // not the timestamp when the counter or an individual data point was created.
    String expected =
        ""
            + "# TYPE requests counter\n"
            + "requests_total{animal=\"bear\",otel_scope_name=\"test\"} 3.0\n"
            + "requests_created{animal=\"bear\",otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "requests_total{animal=\"mouse\",otel_scope_name=\"test\"} 2.0\n"
            + "requests_created{animal=\"mouse\",otel_scope_name=\"test\"} "
            + createdTimestamp
            + "\n"
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(reader.collect())).isEqualTo(expected);
  }

  @Test
  void otelScopeComplete() throws IOException {
    // There is currently no API for adding scope attributes.
    // However, we can at least test the otel_scope_version attribute.
    Meter meter =
        SdkMeterProvider.builder()
            .setClock(testClock)
            .registerMetricReader(this.reader)
            .setResource(
                Resource.getDefault().toBuilder().put("telemetry.sdk.version", "1.x.x").build())
            .build()
            .meterBuilder("test-scope")
            .setInstrumentationVersion("a.b.c")
            .build();
    LongCounter counter = meter.counterBuilder("test.count").build();
    counter.add(1);
    String expected =
        ""
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# TYPE test_count counter\n"
            + "test_count_total{otel_scope_name=\"test-scope\",otel_scope_version=\"a.b.c\"} 1.0\n"
            + "test_count_created{otel_scope_name=\"test-scope\",otel_scope_version=\"a.b.c\"} "
            + createdTimestamp
            + "\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(reader.collect())).isEqualTo(expected);
  }

  @Test
  void otelScopeLabelsOnly() throws IOException {
    PrometheusMetricReader reader =
        new PrometheusMetricReader(/* allowedResourceAttributesFilter= */ null);
    Meter meter =
        SdkMeterProvider.builder()
            .setClock(testClock)
            .registerMetricReader(reader)
            .setResource(
                Resource.getDefault().toBuilder().put("telemetry.sdk.version", "1.x.x").build())
            .build()
            .meterBuilder("test-scope")
            .setInstrumentationVersion("a.b.c")
            .build();
    LongCounter counter = meter.counterBuilder("test.count").build();
    counter.add(1);
    String expected =
        ""
            + "# TYPE target info\n"
            + "target_info{service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# TYPE test_count counter\n"
            + "test_count_total{otel_scope_name=\"test-scope\",otel_scope_version=\"a.b.c\"} 1.0\n"
            + "test_count_created{otel_scope_name=\"test-scope\",otel_scope_version=\"a.b.c\"} "
            + createdTimestamp
            + "\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(reader.collect())).isEqualTo(expected);
  }

  @SuppressWarnings("resource")
  @Test
  void addResourceAttributesWorks() throws IOException {
    PrometheusMetricReader reader =
        new PrometheusMetricReader(/* allowedResourceAttributesFilter= */ Predicates.is("cluster"));
    Meter meter =
        SdkMeterProvider.builder()
            .setClock(testClock)
            .registerMetricReader(reader)
            .setResource(
                Resource.getDefault().toBuilder()
                    .put("cluster", "my.cluster")
                    .put("telemetry.sdk.version", "1.x.x")
                    .build())
            .build()
            .meterBuilder("test-scope")
            .setInstrumentationVersion("a.b.c")
            .build();
    LongCounter counter = meter.counterBuilder("test.count").build();
    counter.add(1);
    String expected =
        ""
            + "# TYPE target info\n"
            + "target_info{cluster=\"my.cluster\",service_name=\"unknown_service:java\",telemetry_sdk_language=\"java\",telemetry_sdk_name=\"opentelemetry\",telemetry_sdk_version=\"1.x.x\"} 1\n"
            + "# TYPE test_count counter\n"

            // In both those metrics we expect the "cluster" label to exist
            + "test_count_total{cluster=\"my.cluster\",otel_scope_name=\"test-scope\",otel_scope_version=\"a.b.c\"} 1.0\n"
            + "test_count_created{cluster=\"my.cluster\",otel_scope_name=\"test-scope\",otel_scope_version=\"a.b.c\"} "
            + createdTimestamp
            + "\n"
            + "# EOF\n";
    assertThat(toOpenMetrics(reader.collect())).isEqualTo(expected);
  }

  @SuppressWarnings("deprecation") // test deprecated constructor
  @Test
  void deprecatedConstructor() {
    assertThat(new PrometheusMetricReader(false, null))
        .usingRecursiveComparison()
        .isEqualTo(new PrometheusMetricReader(null));
  }

  /**
   * Unfortunately there is no easy way to use {@link TestClock} for Exemplar timestamps. Test if
   * {@code expected} equals {@code actual} but {@code <timestamp>} matches arbitrary timestamps.
   */
  private static void assertMatches(String expected, String actual) {
    String regex = toPattern(expected);
    assertThat(actual)
        .as("Expected: " + expected + "\nActual: " + actual)
        .matches(Pattern.compile(regex));
  }

  /**
   * Replace non-deterministic portions of {@code expected} with regex patterns. Other portions are
   * quoted such that must match exactly. The following sequences are replaced:
   *
   * <ul>
   *   <li>{@code <timestamp>}
   *   <li>{@code <spanId>}
   *   <li>{@code <traceId>}
   *   <li>{@code <measurement>}
   * </ul>
   */
  private static String toPattern(String expected) {
    Map<String, String> replacePatterns = new HashMap<>();
    String timestampPattern = "[0-9]+(\\.[0-9]+)?";
    replacePatterns.put("timestamp", timestampPattern);
    replacePatterns.put("maybeNanos", String.format("(nanos: %s)?", timestampPattern));
    replacePatterns.put("spanId", "[a-z0-9]*");
    replacePatterns.put("traceId", "[a-z0-9]*");
    replacePatterns.put("measurement", "[0-9\\.]*");

    Matcher matcher = Pattern.compile("\\<([a-zA-Z]*)\\>").matcher(expected);
    if (!matcher.find()) {
      return Pattern.quote(expected);
    }
    int offset = 0;
    StringBuilder regexBuilder = new StringBuilder();
    do {
      MatchResult matchResult = matcher.toMatchResult();
      String key = matchResult.group(1);
      String pattern = replacePatterns.getOrDefault(key, key);
      regexBuilder
          .append(Pattern.quote(expected.substring(offset, matchResult.start())))
          .append(pattern);
      offset = matchResult.end();
    } while (matcher.find());
    if (offset != expected.length()) {
      regexBuilder.append(Pattern.quote(expected.substring(offset)));
    }
    return regexBuilder.toString();
  }

  private static String toOpenMetrics(MetricSnapshots snapshots) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
    writer.write(out, snapshots);
    return out.toString(StandardCharsets.UTF_8.name());
  }

  private static String toPrometheusProtobuf(MetricSnapshots snapshots) {
    PrometheusProtobufWriter writer = new PrometheusProtobufWriter();
    return writer.toDebugString(snapshots);
  }

  private static String convertTimestamp(long nanoTime) {
    String millis = Long.toString(TimeUnit.NANOSECONDS.toMillis(nanoTime));
    return millis.substring(0, millis.length() - 3) + "." + millis.substring(millis.length() - 3);
  }
}
