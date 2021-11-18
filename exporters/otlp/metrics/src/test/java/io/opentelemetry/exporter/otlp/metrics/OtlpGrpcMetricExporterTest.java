/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporterBuilder;
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
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

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

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void validConfig() {
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setTimeout(0, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setTimeout(Duration.ofMillis(0)))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setTimeout(10, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setTimeout(Duration.ofMillis(10)))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setEndpoint("http://localhost:4317"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setEndpoint("http://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setEndpoint("https://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setEndpoint("http://foo:bar@localhost"))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setCompression("gzip"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpGrpcMetricExporter.builder().setCompression("none"))
        .doesNotThrowAnyException();

    assertThatCode(
            () -> OtlpGrpcMetricExporter.builder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpGrpcMetricExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpGrpcMetricExporter.builder()
                    .setPreferredTemporality(AggregationTemporality.DELTA))
        .doesNotThrowAnyException();
    assertThat(
            OtlpGrpcMetricExporter.builder()
                .setPreferredTemporality(AggregationTemporality.DELTA)
                .build()
                .getPreferredTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(OtlpGrpcMetricExporter.builder().build().getPreferredTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost")
        .hasCauseInstanceOf(URISyntaxException.class);
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");

    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setPreferredTemporality(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("preferredTemporality");
  }

  @Test
  void usingOkHttp() {
    assertThat(OtlpGrpcMetricExporter.builder().delegate)
        .isInstanceOf(OkHttpGrpcExporterBuilder.class);
  }
}
