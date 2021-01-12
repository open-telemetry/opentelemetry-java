/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePoint;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPoint;
import io.opentelemetry.sdk.metrics.data.LongPoint;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link LoggingMetricExporter}. */
class LoggingMetricExporterTest {

  LoggingMetricExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = new LoggingMetricExporter();
  }

  @AfterEach
  void tearDown() {
    exporter.shutdown();
  }

  @Test
  void testExport() {
    long nowEpochNanos = System.currentTimeMillis() * 1000 * 1000;
    Resource resource = Resource.create(Attributes.of(stringKey("host"), "localhost"));
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create("manualInstrumentation", "1.0");
    exporter.export(
        Arrays.asList(
            MetricData.createDoubleSummary(
                resource,
                instrumentationLibraryInfo,
                "measureOne",
                "A summarized test measure",
                "ms",
                DoubleSummaryData.create(
                    Collections.singletonList(
                        DoubleSummaryPoint.create(
                            nowEpochNanos,
                            nowEpochNanos + 245,
                            Labels.of("a", "b", "c", "d"),
                            1010,
                            50000,
                            Arrays.asList(
                                ValueAtPercentile.create(0.0, 25),
                                ValueAtPercentile.create(100.0, 433)))))),
            MetricData.createLongSum(
                resource,
                instrumentationLibraryInfo,
                "counterOne",
                "A simple counter",
                "one",
                LongSumData.create(
                    true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPoint.create(
                            nowEpochNanos,
                            nowEpochNanos + 245,
                            Labels.of("z", "y", "x", "w"),
                            1010)))),
            MetricData.createDoubleSum(
                resource,
                instrumentationLibraryInfo,
                "observedValue",
                "an observer gauge",
                "kb",
                DoubleSumData.create(
                    true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        DoublePoint.create(
                            nowEpochNanos,
                            nowEpochNanos + 245,
                            Labels.of("1", "2", "3", "4"),
                            33.7767))))));
  }

  @Test
  void testFlush() {
    final AtomicBoolean flushed = new AtomicBoolean(false);
    Logger.getLogger(LoggingMetricExporter.class.getName())
        .addHandler(
            new StreamHandler(System.err, new SimpleFormatter()) {
              @Override
              public synchronized void flush() {
                flushed.set(true);
              }
            });
    exporter.flush();
    assertThat(flushed.get()).isTrue();
  }
}
