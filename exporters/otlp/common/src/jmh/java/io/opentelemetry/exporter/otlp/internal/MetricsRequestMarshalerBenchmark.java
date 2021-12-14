/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.internal.metrics.MetricsRequestMarshaler;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class MetricsRequestMarshalerBenchmark {

  private static final Collection<MetricData> METRICS;

  static {
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.newMetricReaderFactory(exporter))
            .setResource(
                Resource.create(
                    Attributes.builder()
                        .put(AttributeKey.booleanKey("key_bool"), true)
                        .put(AttributeKey.stringKey("key_string"), "string")
                        .put(AttributeKey.longKey("key_int"), 100L)
                        .put(AttributeKey.doubleKey("key_double"), 100.3)
                        .put(
                            AttributeKey.stringArrayKey("key_string_array"),
                            Arrays.asList("string", "string"))
                        .put(AttributeKey.longArrayKey("key_long_array"), Arrays.asList(12L, 23L))
                        .put(
                            AttributeKey.doubleArrayKey("key_double_array"),
                            Arrays.asList(12.3, 23.1))
                        .put(
                            AttributeKey.booleanArrayKey("key_boolean_array"),
                            Arrays.asList(true, false))
                        .build()))
            .build();

    Meter meter1 = meterProvider.get("longinstrumentation");
    meter1
        .gaugeBuilder("gauge")
        .setDescription("gauge description")
        .setUnit("unit")
        .ofLongs()
        .buildWithCallback(
            measurement ->
                measurement.record(5, Attributes.of(AttributeKey.stringKey("key"), "value")));
    LongCounter longCounter =
        meter1
            .counterBuilder("counter")
            .setDescription("counter description")
            .setUnit("unit")
            .build();
    longCounter.add(1);
    longCounter.add(2, Attributes.of(AttributeKey.longKey("lives"), 9L));
    longCounter.add(3);
    LongUpDownCounter longUpDownCounter =
        meter1
            .upDownCounterBuilder("updowncounter")
            .setDescription("updowncounter description")
            .setUnit("unit")
            .build();
    longUpDownCounter.add(1);
    longUpDownCounter.add(-1, Attributes.of(AttributeKey.booleanKey("on"), true));
    longUpDownCounter.add(1);

    Meter meter2 = meterProvider.get("doubleinstrumentation");
    meter2
        .gaugeBuilder("doublegauge")
        .setDescription("doublegauge")
        .setUnit("unit")
        .buildWithCallback(measurement -> measurement.record(5.0));
    DoubleCounter doubleCounter = meter2.counterBuilder("doublecounter").ofDoubles().build();
    doubleCounter.add(1.0);
    doubleCounter.add(2.0);
    DoubleUpDownCounter doubleUpDownCounter =
        meter2.upDownCounterBuilder("doubleupdown").ofDoubles().build();
    doubleUpDownCounter.add(1.0);
    doubleUpDownCounter.add(-1.0);
    DoubleHistogram histogram = meter2.histogramBuilder("histogram").build();
    histogram.record(1.0);
    histogram.record(2.0);
    histogram.record(3.0);
    histogram.record(4.0);
    histogram.record(5.0);

    meterProvider.forceFlush().join(10, TimeUnit.SECONDS);
    METRICS = exporter.getFinishedMetricItems();
  }

  @Benchmark
  public ByteArrayOutputStream marshaler() throws IOException {
    MetricsRequestMarshaler marshaler = MetricsRequestMarshaler.create(METRICS);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    marshaler.writeBinaryTo(bos);
    return bos;
  }
}
