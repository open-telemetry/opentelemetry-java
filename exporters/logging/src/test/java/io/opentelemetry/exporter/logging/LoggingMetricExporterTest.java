/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(LoggingMetricExporter.class)
class LoggingMetricExporterTest {

  private static final MetricData METRIC_DATA =
      ImmutableMetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("host"), "localhost")),
          InstrumentationScopeInfo.builder("manualInstrumentation").setVersion("1.0").build(),
          "counterOne",
          "A simple counter",
          "one",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableLongPointData.create(
                      TimeUnit.MILLISECONDS.toNanos(Instant.now().toEpochMilli()),
                      TimeUnit.MILLISECONDS.toNanos(Instant.now().plusMillis(245).toEpochMilli()),
                      Attributes.of(stringKey("z"), "y", stringKey("x"), "w"),
                      1010))));

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(LoggingMetricExporter.class);

  private LoggingMetricExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = LoggingMetricExporter.create();
  }

  @Test
  void preferredTemporality() {
    assertThat(LoggingMetricExporter.create().getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            LoggingMetricExporter.create(AggregationTemporality.DELTA)
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void export() {
    assertThat(exporter.export(Collections.singletonList(METRIC_DATA)).isSuccess()).isTrue();
    assertThat(logs.getEvents())
        .satisfiesExactly(
            loggingEvent ->
                assertThat(loggingEvent.getMessage())
                    .isEqualTo("Received a collection of 1 metrics for export."),
            loggingEvent -> {
              assertThat(loggingEvent.getMessage()).isEqualTo("metric: {0}");
              assertThat(loggingEvent.getArgumentArray()).isEqualTo(new MetricData[] {METRIC_DATA});
            });
  }

  @Test
  void flush() {
    AtomicBoolean flushed = new AtomicBoolean(false);
    Logger.getLogger(LoggingMetricExporter.class.getName())
        .addHandler(
            new StreamHandler(new PrintStream(new ByteArrayOutputStream()), new SimpleFormatter()) {
              @Override
              public synchronized void flush() {
                flushed.set(true);
              }
            });
    exporter.flush();
    assertThat(flushed.get()).isTrue();
  }

  @Test
  void shutdown() {
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(
            exporter
                .export(Collections.singletonList(METRIC_DATA))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }
}
