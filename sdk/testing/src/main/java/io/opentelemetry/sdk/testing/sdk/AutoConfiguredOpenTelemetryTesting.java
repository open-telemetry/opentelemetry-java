/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.sdk;

import static org.awaitility.Awaitility.await;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.internal.AutoConfigureUtil;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.testing.assertj.TraceAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AutoConfiguredOpenTelemetryTesting extends OpenTelemetryTesting {

  static final String MEMORY_EXPORTER = "memory";

  public static AutoConfiguredOpenTelemetryTesting create(Map<String, String> properties) {
    return new AutoConfiguredOpenTelemetryTesting(getOpenTelemetrySdk(properties));
  }

  private AutoConfiguredOpenTelemetryTesting(OpenTelemetryTestSdk sdk) {
    super(sdk);
  }

  private static OpenTelemetryTestSdk getOpenTelemetrySdk(Map<String, String> properties) {

    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    InMemoryMetricExporter metricExporter =
        InMemoryMetricExporter.create(AggregationTemporality.DELTA);
    InMemoryLogRecordExporter logRecordExporter = InMemoryLogRecordExporter.create();

    AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(AutoConfiguredOpenTelemetryTesting::getProperties)
            .addPropertiesSupplier(() -> properties);
    AutoConfigureUtil.setComponentLoader(
        sdkBuilder, new InMemoryComponentLoader(spanExporter, metricExporter, logRecordExporter));
    OpenTelemetrySdk openTelemetrySdk = sdkBuilder.build().getOpenTelemetrySdk();
    return new OpenTelemetryTestSdk(
        openTelemetrySdk, spanExporter, metricExporter::getFinishedMetricItems, logRecordExporter);
  }

  private static Map<String, String> getProperties() {
    Map<String, String> map = new HashMap<>();
    map.put("otel.bsp.schedule.delay", "10");
    map.put("otel.metric.export.interval", "10");
    map.put("otel.blrp.schedule.delay", "10");
    map.put("otel.traces.exporter", MEMORY_EXPORTER);
    map.put("otel.metrics.exporter", MEMORY_EXPORTER);
    map.put("otel.logs.exporter", MEMORY_EXPORTER);
    return map;
  }

  /** needs org.awaitility:awaitility to be on the classpath */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final void assertTraces(Consumer<TraceAssert>... assertions) {
    await()
        .atMost(Duration.ofSeconds(1))
        .untilAsserted(() -> assertTraces().hasTracesSatisfyingExactly(assertions));
  }
}
