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

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.TemporalQuality;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/** Tests for the {@link LoggingMetricExporter}. */
public class LoggingMetricExporterTest {

  @Test
  public void testExport() {
    LoggingMetricExporter exporter = new LoggingMetricExporter();

    long nowEpochNanos = System.currentTimeMillis() * 1000 * 1000;
    Resource resource =
        Resource.create(ImmutableMap.of("host", AttributeValue.stringAttributeValue("localhost")));
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create("manualInstrumentation", "1.0");
    exporter.export(
        Arrays.asList(
            MetricData.create(
                Descriptor.create(
                    "measureOne",
                    "A summarized test measure",
                    "ms",
                    Type.SUMMARY,
                    ImmutableMap.of("foo", "bar", "baz", "zoom"),
                    TemporalQuality.CUMULATIVE),
                resource,
                instrumentationLibraryInfo,
                Collections.<Point>singletonList(
                    SummaryPoint.create(
                        nowEpochNanos,
                        nowEpochNanos + 245,
                        ImmutableMap.of("a", "b", "c", "d"),
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
                    Type.MONOTONIC_LONG,
                    ImmutableMap.of("alpha", "aleph", "beta", "bet"),
                    TemporalQuality.CUMULATIVE),
                resource,
                instrumentationLibraryInfo,
                Collections.<Point>singletonList(
                    LongPoint.create(
                        nowEpochNanos,
                        nowEpochNanos + 245,
                        ImmutableMap.of("z", "y", "x", "w"),
                        1010))),
            MetricData.create(
                Descriptor.create(
                    "observedValue",
                    "an observer gauge",
                    "kb",
                    Type.NON_MONOTONIC_DOUBLE,
                    ImmutableMap.of("uno", "eins", "dos", "zwei"),
                    TemporalQuality.CUMULATIVE),
                resource,
                instrumentationLibraryInfo,
                Collections.<Point>singletonList(
                    DoublePoint.create(
                        nowEpochNanos,
                        nowEpochNanos + 245,
                        ImmutableMap.of("1", "2", "3", "4"),
                        33.7767)))));
  }
}
