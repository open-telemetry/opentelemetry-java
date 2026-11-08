/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.metrics.ExporterInstrumentation;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MetricReusableDataMarshalerTest {

  @ParameterizedTest
  @MethodSource("exportCases")
  void countsDataPoints(
      InternalTelemetryVersion telemetryVersion, MemoryMode memoryMode, boolean successful) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    try (SdkMeterProvider provider =
        SdkMeterProvider.builder().registerMetricReader(reader).build()) {
      ExporterInstrumentation instrumentation =
          new ExporterInstrumentation(
              telemetryVersion,
              () -> provider,
              ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_GRPC_METRIC_EXPORTER),
              URI.create("http://localhost:4317"));
      AtomicReference<CompletableResultCode> pending = new AtomicReference<>();
      MetricReusableDataMarshaler marshaler =
          new MetricReusableDataMarshaler(
              memoryMode,
              (request, count) -> {
                ExporterInstrumentation.Recording recording =
                    instrumentation.startRecordingExport(count);
                CompletableResultCode result = new CompletableResultCode();
                pending.set(result);
                return result.whenComplete(
                    () -> {
                      if (result.isSuccess()) {
                        recording.finishSuccessful();
                      } else {
                        recording.finishFailed("test_failure");
                      }
                    });
              });
      long exported = 0;
      int[] expectedPointCounts = {5, 0, 0, 1};
      int batchIndex = 0;
      for (List<MetricData> batch :
          Arrays.asList(
              Arrays.asList(metric("first", 3), metric("second", 2), metric("empty", 0)),
              Collections.singletonList(metric("empty", 0)),
              Collections.<MetricData>emptyList(),
              Collections.singletonList(metric("next", 1)))) {
        int points = expectedPointCounts[batchIndex++];
        CompletableResultCode result = marshaler.export(batch);
        assertThat(result.isDone()).isFalse();
        if (telemetryVersion == InternalTelemetryVersion.LEGACY) {
          assertLegacyMetricValue(
              reader,
              "otlp.exporter.seen",
              exported + points,
              Attributes.builder().put("type", "metric").build());
          if (batchIndex == 1) {
            assertThat(reader.collectAllMetrics())
                .noneMatch(metric -> metric.getName().equals("otlp.exporter.exported"));
          } else {
            assertLegacyMetricValue(
                reader,
                "otlp.exporter.exported",
                exported,
                Attributes.builder().put("type", "metric").put("success", successful).build());
          }
        } else {
          assertMetricValue(reader, "otel.sdk.exporter.metric_data_point.inflight", points);
        }
        if (successful) {
          pending.get().succeed();
        } else {
          pending.get().fail();
        }
        assertThat(result.isDone()).isTrue();
        assertThat(result.isSuccess()).isEqualTo(successful);
        exported += points;
        if (telemetryVersion == InternalTelemetryVersion.LEGACY) {
          assertLegacyMetricValue(
              reader,
              "otlp.exporter.seen",
              exported,
              Attributes.builder().put("type", "metric").build());
          assertLegacyMetricValue(
              reader,
              "otlp.exporter.exported",
              exported,
              Attributes.builder().put("type", "metric").put("success", successful).build());
        } else {
          assertMetricValue(reader, "otel.sdk.exporter.metric_data_point.inflight", 0);
          assertMetricValue(reader, "otel.sdk.exporter.metric_data_point.exported", exported);
        }
      }
    }
  }

  private static void assertMetricValue(InMemoryMetricReader reader, String name, long expected) {
    assertThat(reader.collectAllMetrics())
        .filteredOn(metric -> metric.getName().equals(name))
        .singleElement()
        .satisfies(
            metric ->
                assertThat(metric.getLongSumData().getPoints())
                    .singleElement()
                    .extracting(LongPointData::getValue)
                    .isEqualTo(expected));
  }

  private static void assertLegacyMetricValue(
      InMemoryMetricReader reader, String name, long expected, Attributes attributes) {
    assertThat(reader.collectAllMetrics())
        .filteredOn(metric -> metric.getName().equals(name))
        .singleElement()
        .satisfies(
            metric ->
                assertThat(metric)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point -> point.hasValue(expected).hasAttributes(attributes))));
  }

  private static MetricData metric(String name, int count) {
    List<LongPointData> points = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      points.add(ImmutableLongPointData.create(0, 1, Attributes.builder().put("id", i).build(), i));
    }
    return ImmutableMetricData.createLongGauge(
        Resource.empty(),
        InstrumentationScopeInfo.create("test"),
        name,
        "",
        "1",
        ImmutableGaugeData.create(points));
  }

  private static Stream<Arguments> exportCases() {
    return Stream.of(InternalTelemetryVersion.LEGACY, InternalTelemetryVersion.LATEST)
        .flatMap(
            version ->
                Arrays.stream(MemoryMode.values())
                    .flatMap(
                        mode ->
                            Stream.of(
                                Arguments.argumentSet(
                                    version + " " + mode + " success", version, mode, true),
                                Arguments.argumentSet(
                                    version + " " + mode + " failure", version, mode, false))));
  }
}
