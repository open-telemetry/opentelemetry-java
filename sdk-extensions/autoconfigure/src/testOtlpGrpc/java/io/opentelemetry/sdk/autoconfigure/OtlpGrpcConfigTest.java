/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.google.common.collect.Lists;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpGrpcConfigTest {

  private static final List<SpanData> SPAN_DATA =
      Lists.newArrayList(
          TestSpanData.builder()
              .setHasEnded(true)
              .setName("name")
              .setStartEpochNanos(MILLISECONDS.toNanos(System.currentTimeMillis()))
              .setEndEpochNanos(MILLISECONDS.toNanos(System.currentTimeMillis()))
              .setKind(SpanKind.SERVER)
              .setStatus(StatusData.error())
              .setTotalRecordedEvents(0)
              .setTotalRecordedLinks(0)
              .build());
  private static final List<MetricData> METRIC_DATA =
      Lists.newArrayList(
          MetricData.createLongSum(
              Resource.empty(),
              InstrumentationLibraryInfo.empty(),
              "metric_name",
              "metric_description",
              "ms",
              LongSumData.create(
                  false,
                  AggregationTemporality.CUMULATIVE,
                  Collections.singletonList(
                      LongPointData.create(
                          MILLISECONDS.toNanos(System.currentTimeMillis()),
                          MILLISECONDS.toNanos(System.currentTimeMillis()),
                          Attributes.of(stringKey("key"), "value"),
                          10)))));

  @RegisterExtension
  @Order(1)
  public static final SelfSignedCertificateExtension certificate =
      new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(2)
  public static final OtlpGrpcServerExtension server = new OtlpGrpcServerExtension(certificate);

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @AfterEach
  public void tearDown() {
    server.reset();
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void configureExportersGeneral() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.endpoint", "https://localhost:" + server.httpsPort());
    props.put("otel.exporter.otlp.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.exporter.otlp.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.compression", "gzip");
    props.put("otel.exporter.otlp.timeout", "15s");
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter("otlp", properties, Collections.emptyMap());
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(properties, SdkMeterProvider.builder());

    assertThat(spanExporter)
        .extracting("delegate.timeoutNanos")
        .isEqualTo(TimeUnit.SECONDS.toNanos(15));
    assertThat(spanExporter.export(SPAN_DATA).join(15, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(server.traceRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(
                        ":path", "/opentelemetry.proto.collector.trace.v1.TraceService/Export")
                    && headers.contains("header-key", "header-value")
                    && headers.contains("grpc-encoding", "gzip"));

    assertThat(metricExporter)
        .extracting("delegate.timeoutNanos")
        .isEqualTo(TimeUnit.SECONDS.toNanos(15));
    assertThat(metricExporter.export(METRIC_DATA).join(15, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(server.metricRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(
                        ":path", "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export")
                    && headers.contains("header-key", "header-value")
                    && headers.contains("grpc-encoding", "gzip"));
  }

  @Test
  void configureSpanExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.compression", "foo");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.traces.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.traces.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.exporter.otlp.traces.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.traces.compression", "gzip");
    props.put("otel.exporter.otlp.traces.timeout", "15s");
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp", DefaultConfigProperties.createForTest(props), Collections.emptyMap());

    assertThat(spanExporter)
        .extracting("delegate.timeoutNanos")
        .isEqualTo(TimeUnit.SECONDS.toNanos(15));
    assertThat(spanExporter.export(SPAN_DATA).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(server.traceRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(
                        ":path", "/opentelemetry.proto.collector.trace.v1.TraceService/Export")
                    && headers.contains("header-key", "header-value"));
  }

  @Test
  public void configureMetricExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.compression", "gzip");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.metrics.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.metrics.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.exporter.otlp.metrics.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.metrics.compression", "gzip");
    props.put("otel.exporter.otlp.metrics.timeout", "15s");
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            DefaultConfigProperties.createForTest(props), SdkMeterProvider.builder());

    assertThat(metricExporter)
        .extracting("delegate.timeoutNanos")
        .isEqualTo(TimeUnit.SECONDS.toNanos(15));
    assertThat(metricExporter.export(METRIC_DATA).join(15, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(server.metricRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(
                        ":path", "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export")
                    && headers.contains("header-key", "header-value")
                    && headers.contains("grpc-encoding", "gzip"));
  }

  @Test
  void configureTlsInvalidCertificatePath() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);

    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp", properties, Collections.emptyMap()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate path:");

    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureOtlpMetrics(
                    properties, SdkMeterProvider.builder()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate path:");
  }

  @Test
  void configuresGlobal() {
    System.setProperty("otel.exporter.otlp.endpoint", "https://localhost:" + server.httpsPort());
    System.setProperty(
        "otel.exporter.otlp.certificate", certificate.certificateFile().getAbsolutePath());
    System.setProperty("otel.imr.export.interval", "1s");

    GlobalOpenTelemetry.get().getTracer("test").spanBuilder("test").startSpan().end();

    await()
        .untilAsserted(
            () -> {
              assertThat(server.traceRequests).hasSize(1);

              // Not well defined how many metric exports would have happened by now, check that
              // any did. Metrics are recorded by OtlpGrpcSpanExporter, BatchSpanProcessor, and
              // potentially others.
              assertThat(server.metricRequests).isNotEmpty();
            });
  }
}
