/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

class OtlpGrpcMetricExporterTest
    extends AbstractGrpcTelemetryExporterTest<MetricData, ResourceMetrics, OtlpGrpcMetricExporter> {

  OtlpGrpcMetricExporterTest() {
    super("metric", ResourceMetrics.getDefaultInstance());
  }

  @Override
  protected OtlpGrpcMetricExporter createExporter(String endpoint) {
    return OtlpGrpcMetricExporter.builder().setEndpoint(endpoint).build();
  }

  @Override
  protected OtlpGrpcMetricExporter createExporterWithTimeout(String endpoint, Duration timeout) {
    return OtlpGrpcMetricExporter.builder().setEndpoint(endpoint).setTimeout(timeout).build();
  }

  @Override
  protected CompletableResultCode shutdownExporter(OtlpGrpcMetricExporter exporter) {
    return exporter.shutdown();
  }

  @Override
  protected CompletableResultCode doExport(
      OtlpGrpcMetricExporter exporter, List<MetricData> telemetry) {
    return exporter.export(telemetry);
  }

  @Override
  protected MetricData generateFakeTelemetry() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return MetricData.createLongSum(
        Resource.empty(),
        InstrumentationLibraryInfo.empty(),
        "name",
        "description",
        "1",
        LongSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                LongPointData.create(startNs, endNs, Attributes.of(stringKey("k"), "v"), 5))));
  }

  @Override
  protected Marshaler[] toMarshalers(List<MetricData> telemetry) {
    return ResourceMetricsMarshaler.create(telemetry);
  }
}
