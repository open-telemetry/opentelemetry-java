/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpHttpServerExtension.generateFakeLog;
import static io.opentelemetry.sdk.autoconfigure.OtlpHttpServerExtension.generateFakeMetric;
import static io.opentelemetry.sdk.autoconfigure.OtlpHttpServerExtension.generateFakeSpan;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.google.common.collect.Lists;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpHttpConfigTest {

  @RegisterExtension
  public static final OtlpHttpServerExtension server = new OtlpHttpServerExtension();

  @BeforeEach
  void setUp() {
    server.reset();
    GlobalOpenTelemetry.resetForTest();
  }

  @AfterEach
  public void tearDown() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void configureExportersGeneral() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.protocol", "http/protobuf");
    props.put("otel.exporter.otlp.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.certificate", server.selfSignedCertificate.certificate().getPath());
    props.put("otel.exporter.otlp.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.compression", "gzip");
    props.put("otel.exporter.otlp.timeout", "15s");
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp", properties, Collections.emptyMap(), MeterProvider.noop());
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(properties, SdkMeterProvider.builder());
    LogExporter logExporter =
        LogExporterConfiguration.configureOtlpLogs(properties, MeterProvider.noop());

    assertThat(spanExporter)
        .extracting("delegate.client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            spanExporter
                .export(Lists.newArrayList(generateFakeSpan()))
                .join(15, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(server.traceRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/traces")
                    && headers.contains("header-key", "header-value")
                    && headers.contains("content-encoding", "gzip"));

    assertThat(metricExporter)
        .extracting("delegate.client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            metricExporter
                .export(Lists.newArrayList(generateFakeMetric()))
                .join(15, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(server.metricRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/metrics")
                    && headers.contains("header-key", "header-value")
                    && headers.contains("content-encoding", "gzip"));

    assertThat(logExporter)
        .extracting("delegate.client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            logExporter
                .export(Lists.newArrayList(generateFakeLog()))
                .join(15, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(server.logRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/logs")
                    && headers.contains("header-key", "header-value")
                    && headers.contains("content-encoding", "gzip"));
  }

  @Test
  void configureSpanExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.protocol", "grpc");
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.compression", "foo");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.traces.protocol", "http/protobuf");
    props.put(
        "otel.exporter.otlp.traces.endpoint",
        "https://localhost:" + server.httpsPort() + "/v1/traces");
    props.put(
        "otel.exporter.otlp.traces.certificate",
        server.selfSignedCertificate.certificate().getPath());
    props.put("otel.exporter.otlp.traces.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.traces.compression", "gzip");
    props.put("otel.exporter.otlp.traces.timeout", "15s");
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            DefaultConfigProperties.createForTest(props),
            Collections.emptyMap(),
            MeterProvider.noop());

    assertThat(spanExporter)
        .extracting("delegate.client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            spanExporter
                .export(Lists.newArrayList(generateFakeSpan()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(server.traceRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/traces")
                    && headers.contains("header-key", "header-value")
                    && headers.contains("content-encoding", "gzip"));
  }

  @Test
  public void configureMetricExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.protocol", "grpc");
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.compression", "foo");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.metrics.protocol", "http/protobuf");
    props.put(
        "otel.exporter.otlp.metrics.endpoint",
        "https://localhost:" + server.httpsPort() + "/v1/metrics");
    props.put(
        "otel.exporter.otlp.metrics.certificate",
        server.selfSignedCertificate.certificate().getPath());
    props.put("otel.exporter.otlp.metrics.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.metrics.compression", "gzip");
    props.put("otel.exporter.otlp.metrics.timeout", "15s");
    props.put("otel.exporter.otlp.metrics.temporality", "DELTA");
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            DefaultConfigProperties.createForTest(props), SdkMeterProvider.builder());

    assertThat(metricExporter)
        .extracting("delegate.client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(metricExporter.getPreferredTemporality()).isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            metricExporter
                .export(Lists.newArrayList(generateFakeMetric()))
                .join(15, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(server.metricRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/metrics")
                    && headers.contains("header-key", "header-value"));
  }

  @Test
  public void configureLogExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.protocol", "grpc");
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.compression", "foo");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.logs.protocol", "http/protobuf");
    props.put(
        "otel.exporter.otlp.logs.endpoint", "https://localhost:" + server.httpsPort() + "/v1/logs");
    props.put(
        "otel.exporter.otlp.logs.certificate",
        server.selfSignedCertificate.certificate().getPath());
    props.put("otel.exporter.otlp.logs.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.logs.compression", "gzip");
    props.put("otel.exporter.otlp.logs.timeout", "15s");
    LogExporter logExporter =
        LogExporterConfiguration.configureOtlpLogs(
            DefaultConfigProperties.createForTest(props), MeterProvider.noop());

    assertThat(logExporter)
        .extracting("delegate.client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            logExporter
                .export(Lists.newArrayList(generateFakeLog()))
                .join(15, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(server.logRequests).hasSize(1);
    assertThat(server.requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/logs")
                    && headers.contains("header-key", "header-value"));
  }

  @Test
  void configureTlsInvalidCertificatePath() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.protocol", "http/protobuf");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);

    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp", properties, Collections.emptyMap(), MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate path:");

    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureOtlpMetrics(
                    properties, SdkMeterProvider.builder()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate path:");

    assertThatThrownBy(
            () -> LogExporterConfiguration.configureOtlpLogs(properties, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate path:");
  }

  @Test
  void configuresGlobal() {
    System.setProperty("otel.exporter.otlp.protocol", "http/protobuf");
    System.setProperty(
        "otel.exporter.otlp.endpoint", "https://localhost:" + server.httpsPort() + "/");
    System.setProperty(
        "otel.exporter.otlp.certificate", server.selfSignedCertificate.certificate().getPath());
    System.setProperty("otel.metric.export.interval", "1s");

    GlobalOpenTelemetry.get().getTracer("test").spanBuilder("test").startSpan().end();

    await()
        .untilAsserted(
            () -> {
              assertThat(server.traceRequests).hasSize(1);

              // Not well defined how many metric exports would have happened by now, check that
              // any did. Metrics are recorded by OtlpHttpSpanExporter, BatchSpanProcessor, and
              // potentially others.
              assertThat(server.metricRequests).isNotEmpty();
            });
  }
}
