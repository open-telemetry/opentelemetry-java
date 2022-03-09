/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressLogger(LoggingMetricExporter.class)
class LoggingMetricExporterTest {

  LoggingMetricExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = LoggingMetricExporter.create();
  }

  @AfterEach
  void tearDown() {
    exporter.shutdown();
  }

  @Test
  void preferredTemporality() {
    assertThat(LoggingMetricExporter.create().getPreferredTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(LoggingMetricExporter.create(AggregationTemporality.DELTA).getPreferredTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void testExport() {
    long nowEpochNanos = System.currentTimeMillis() * 1000 * 1000;
    Resource resource = Resource.create(Attributes.of(stringKey("host"), "localhost"));
    InstrumentationScopeInfo instrumentationScopeInfo =
        InstrumentationScopeInfo.create("manualInstrumentation", "1.0", null);
    exporter.export(
        Arrays.asList(
            MetricData.createDoubleSummary(
                resource,
                instrumentationScopeInfo,
                "measureOne",
                "A summarized test measure",
                "ms",
                ImmutableSummaryData.create(
                    Collections.singletonList(
                        ImmutableSummaryPointData.create(
                            nowEpochNanos,
                            nowEpochNanos + 245,
                            Attributes.of(stringKey("a"), "b", stringKey("c"), "d"),
                            1010,
                            50000,
                            Arrays.asList(
                                ImmutableValueAtPercentile.create(0.0, 25),
                                ImmutableValueAtPercentile.create(100.0, 433)))))),
            MetricData.createLongSum(
                resource,
                instrumentationScopeInfo,
                "counterOne",
                "A simple counter",
                "one",
                ImmutableSumData.create(
                    true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        ImmutableLongPointData.create(
                            nowEpochNanos,
                            nowEpochNanos + 245,
                            Attributes.of(stringKey("z"), "y", stringKey("x"), "w"),
                            1010)))),
            MetricData.createDoubleSum(
                resource,
                instrumentationScopeInfo,
                "observedValue",
                "an observer gauge",
                "kb",
                ImmutableSumData.create(
                    true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        ImmutableDoublePointData.create(
                            nowEpochNanos,
                            nowEpochNanos + 245,
                            Attributes.of(stringKey("1"), "2", stringKey("3"), "4"),
                            33.7767))))));
  }

  @Test
  void testFlush() {
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
}
