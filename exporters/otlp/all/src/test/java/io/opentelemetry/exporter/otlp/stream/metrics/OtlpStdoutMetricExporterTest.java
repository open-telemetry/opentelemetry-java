/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.stream.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.common.io.Resources;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.stream.StreamExporter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;

class OtlpStdoutMetricExporterTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

  private static final MetricData METRIC1 =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          InstrumentationScopeInfo.builder("instrumentation")
              .setVersion("1")
              .setAttributes(Attributes.builder().put("key", "value").build())
              .build(),
          "metric1",
          "metric1 description",
          "m",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Arrays.asList(
                  ImmutableDoublePointData.create(
                      1, 2, Attributes.of(stringKey("cat"), "meow"), 4))));

  private static final MetricData METRIC2 =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build(),
          "metric2",
          "metric2 description",
          "s",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Arrays.asList(
                  ImmutableDoublePointData.create(
                      1, 2, Attributes.of(stringKey("cat"), "meow"), 4))));

  private static final ByteArrayOutputStream STREAM = new ByteArrayOutputStream();
  private static final PrintStream PRINT_STREAM = new PrintStream(STREAM);

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(StreamExporter.class);

  MetricExporter exporter;

  private static String logs() {
    try {
      return STREAM.toString(StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeAll
  static void setUpStatic() {
    System.setOut(PRINT_STREAM);
  }

  @BeforeEach
  void setUp() {
    exporter = OtlpStdoutMetricExporter.getDefault();
    STREAM.reset();
  }

  @AfterAll
  @SuppressWarnings("SystemOut")
  static void tearDown() {
    System.setOut(System.out);
  }

  @Test
  void log() throws Exception {
    exporter.export(Arrays.asList(METRIC1, METRIC2));

    String message = logs();
    JSONAssert.assertEquals(
        Resources.toString(Resources.getResource("expected-metrics.json"), StandardCharsets.UTF_8),
        message,
        false);
    assertThat(message).doesNotContain("\n");
  }

  @Test
  void shutdown() {
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(
            exporter
                .export(Collections.singletonList(METRIC1))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(logs()).isEmpty();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  /** Test configuration specific to metric exporter. */
  @Test
  void validMetricConfig() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    assertThat(OtlpStdoutMetricExporter.builder().setOutputStream(stream).build().getOutputStream())
        .isEqualTo(stream);

    assertThatCode(
            () ->
                OtlpStdoutMetricExporter.builder()
                    .setAggregationTemporalitySelector(
                        AggregationTemporalitySelector.deltaPreferred()))
        .doesNotThrowAnyException();
    assertThat(
            OtlpStdoutMetricExporter.builder()
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .build()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            OtlpStdoutMetricExporter.builder()
                .build()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);

    assertThat(
            OtlpStdoutMetricExporter.builder()
                .setDefaultAggregationSelector(
                    DefaultAggregationSelector.getDefault()
                        .with(InstrumentType.HISTOGRAM, Aggregation.drop()))
                .build()
                .getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.drop());
  }
}
