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

package io.opentelemetry.exporters.logging;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
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
    Resource resource =
        Resource.create(Attributes.of("host", AttributeValue.stringAttributeValue("localhost")));
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create("manualInstrumentation", "1.0");
    exporter.export(
        Arrays.asList(
            MetricData.create(
                Descriptor.create(
                    "measureOne",
                    "A summarized test measure",
                    "ms",
                    Descriptor.Type.SUMMARY,
                    Labels.of("foo", "bar", "baz", "zoom")),
                resource,
                instrumentationLibraryInfo,
                Collections.singletonList(
                    SummaryPoint.create(
                        nowEpochNanos,
                        nowEpochNanos + 245,
                        Labels.of("a", "b", "c", "d"),
                        1010,
                        50000,
                        Arrays.asList(
                            ValueAtPercentile.create(0.0, 25),
                            ValueAtPercentile.create(100.0, 433))))),
            MetricData.create(
                Descriptor.create(
                    "counterOne",
                    "A simple counter",
                    "one",
                    Descriptor.Type.MONOTONIC_LONG,
                    Labels.of("alpha", "aleph", "beta", "bet")),
                resource,
                instrumentationLibraryInfo,
                Collections.singletonList(
                    LongPoint.create(
                        nowEpochNanos, nowEpochNanos + 245, Labels.of("z", "y", "x", "w"), 1010))),
            MetricData.create(
                Descriptor.create(
                    "observedValue",
                    "an observer gauge",
                    "kb",
                    Descriptor.Type.NON_MONOTONIC_DOUBLE,
                    Labels.of("uno", "eins", "dos", "zwei")),
                resource,
                instrumentationLibraryInfo,
                Collections.singletonList(
                    DoublePoint.create(
                        nowEpochNanos,
                        nowEpochNanos + 245,
                        Labels.of("1", "2", "3", "4"),
                        33.7767)))));
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
