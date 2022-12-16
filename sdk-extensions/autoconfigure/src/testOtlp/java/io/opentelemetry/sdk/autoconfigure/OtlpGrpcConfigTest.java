/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpGrpcServerExtension.generateFakeLog;
import static io.opentelemetry.sdk.autoconfigure.OtlpGrpcServerExtension.generateFakeMetric;
import static io.opentelemetry.sdk.autoconfigure.OtlpGrpcServerExtension.generateFakeSpan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.INTEGER;
import static org.awaitility.Awaitility.await;

import com.google.common.collect.Lists;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.events.GlobalEventEmitterProvider;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.SetSystemProperty;

class OtlpGrpcConfigTest {

  private static final List<SpanData> SPAN_DATA = Lists.newArrayList(generateFakeSpan());
  private static final List<MetricData> METRIC_DATA = Lists.newArrayList(generateFakeMetric());
  private static final List<LogRecordData> LOG_RECORD_DATA = Lists.newArrayList(generateFakeLog());

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
    GlobalLoggerProvider.resetForTest();
    GlobalEventEmitterProvider.resetForTest();
  }

  @AfterEach
  public void tearDown() {
    server.reset();
    shutdownGlobalSdk();
    GlobalOpenTelemetry.resetForTest();
    GlobalLoggerProvider.resetForTest();
    GlobalEventEmitterProvider.resetForTest();
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
    try (SpanExporter spanExporter =
            SpanExporterConfiguration.configureExporter(
                "otlp", properties, NamedSpiManager.createEmpty(), MeterProvider.noop());
        MetricExporter metricExporter =
            MetricExporterConfiguration.configureOtlpMetrics(properties);
        LogRecordExporter logRecordExporter =
            LogRecordExporterConfiguration.configureOtlpLogs(properties, MeterProvider.noop())) {
      assertThat(spanExporter)
          .extracting("delegate.client.callTimeoutMillis", INTEGER)
          .isEqualTo(TimeUnit.SECONDS.toMillis(15));
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
          .extracting("delegate.client.callTimeoutMillis", INTEGER)
          .isEqualTo(TimeUnit.SECONDS.toMillis(15));
      assertThat(metricExporter.export(METRIC_DATA).join(15, TimeUnit.SECONDS).isSuccess())
          .isTrue();
      assertThat(server.metricRequests).hasSize(1);
      assertThat(server.requestHeaders)
          .anyMatch(
              headers ->
                  headers.contains(
                          ":path",
                          "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export")
                      && headers.contains("header-key", "header-value")
                      && headers.contains("grpc-encoding", "gzip"));

      assertThat(logRecordExporter)
          .extracting("delegate.client.callTimeoutMillis", INTEGER)
          .isEqualTo(TimeUnit.SECONDS.toMillis(15));
      assertThat(logRecordExporter.export(LOG_RECORD_DATA).join(15, TimeUnit.SECONDS).isSuccess())
          .isTrue();
      assertThat(server.logRequests).hasSize(1);
      assertThat(server.requestHeaders)
          .anyMatch(
              headers ->
                  headers.contains(
                          ":path", "/opentelemetry.proto.collector.logs.v1.LogsService/Export")
                      && headers.contains("header-key", "header-value")
                      && headers.contains("grpc-encoding", "gzip"));
    }
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
    try (SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            DefaultConfigProperties.createForTest(props),
            NamedSpiManager.createEmpty(),
            MeterProvider.noop())) {
      assertThat(spanExporter)
          .extracting("delegate.client.callTimeoutMillis", INTEGER)
          .isEqualTo(TimeUnit.SECONDS.toMillis(15));
      assertThat(spanExporter.export(SPAN_DATA).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(server.traceRequests).hasSize(1);
      assertThat(server.requestHeaders)
          .anyMatch(
              headers ->
                  headers.contains(
                          ":path", "/opentelemetry.proto.collector.trace.v1.TraceService/Export")
                      && headers.contains("header-key", "header-value"));
    }
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
    props.put("otel.exporter.otlp.metrics.temporality.preference", "DELTA");
    try (MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            DefaultConfigProperties.createForTest(props))) {

      assertThat(metricExporter)
          .extracting("delegate.client.callTimeoutMillis", INTEGER)
          .isEqualTo(TimeUnit.SECONDS.toMillis(15));
      assertThat(metricExporter.getAggregationTemporality(InstrumentType.COUNTER))
          .isEqualTo(AggregationTemporality.DELTA);
      assertThat(metricExporter.getAggregationTemporality(InstrumentType.UP_DOWN_COUNTER))
          .isEqualTo(AggregationTemporality.CUMULATIVE);
      assertThat(metricExporter.export(METRIC_DATA).join(15, TimeUnit.SECONDS).isSuccess())
          .isTrue();
      assertThat(server.metricRequests).hasSize(1);
      assertThat(server.requestHeaders)
          .anyMatch(
              headers ->
                  headers.contains(
                          ":path",
                          "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export")
                      && headers.contains("header-key", "header-value")
                      && headers.contains("grpc-encoding", "gzip"));
    }
  }

  @Test
  public void configureLogRecordExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.compression", "gzip");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.logs.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.logs.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.exporter.otlp.logs.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.logs.compression", "gzip");
    props.put("otel.exporter.otlp.logs.timeout", "15s");
    try (LogRecordExporter logRecordExporter =
        LogRecordExporterConfiguration.configureOtlpLogs(
            DefaultConfigProperties.createForTest(props), MeterProvider.noop())) {

      assertThat(logRecordExporter)
          .extracting("delegate.client.callTimeoutMillis", INTEGER)
          .isEqualTo(TimeUnit.SECONDS.toMillis(15));
      assertThat(logRecordExporter.export(LOG_RECORD_DATA).join(15, TimeUnit.SECONDS).isSuccess())
          .isTrue();
      assertThat(server.logRequests).hasSize(1);
      assertThat(server.requestHeaders)
          .anyMatch(
              headers ->
                  headers.contains(
                          ":path", "/opentelemetry.proto.collector.logs.v1.LogsService/Export")
                      && headers.contains("header-key", "header-value")
                      && headers.contains("grpc-encoding", "gzip"));
    }
  }

  @Test
  void configureTlsInvalidCertificatePath() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);

    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp", properties, NamedSpiManager.createEmpty(), MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate/key path:");

    assertThatThrownBy(() -> MetricExporterConfiguration.configureOtlpMetrics(properties))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate/key path:");

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureOtlpLogs(properties, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate/key path:");
  }

  @Test
  void configureTlsMissingClientCertificatePath() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.client.key", Paths.get("foo", "bar", "baz").toString());
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);

    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp", properties, NamedSpiManager.createEmpty(), MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Client key provided but certification chain is missing");

    assertThatThrownBy(() -> MetricExporterConfiguration.configureOtlpMetrics(properties))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Client key provided but certification chain is missing");

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureOtlpLogs(properties, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Client key provided but certification chain is missing");
  }

  @Test
  void configureTlsMissingClientKeyPath() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.client.certificate", Paths.get("foo", "bar", "baz").toString());
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);

    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "otlp", properties, NamedSpiManager.createEmpty(), MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Client key chain provided but key is missing");

    assertThatThrownBy(() -> MetricExporterConfiguration.configureOtlpMetrics(properties))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Client key chain provided but key is missing");

    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureOtlpLogs(properties, MeterProvider.noop()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Client key chain provided but key is missing");
  }

  @Test
  @SetSystemProperty(key = "otel.java.global-autoconfigure.enabled", value = "true")
  void configuresGlobal() {
    System.setProperty("otel.exporter.otlp.endpoint", "https://localhost:" + server.httpsPort());
    System.setProperty(
        "otel.exporter.otlp.certificate", certificate.certificateFile().getAbsolutePath());
    System.setProperty("otel.metric.export.interval", "1s");

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

  static void shutdownGlobalSdk() {
    try {
      Field globalOpenTelemetryField =
          GlobalOpenTelemetry.class.getDeclaredField("globalOpenTelemetry");
      globalOpenTelemetryField.setAccessible(true);
      Object globalOpenTelemetry = globalOpenTelemetryField.get(null);
      if (globalOpenTelemetry == null) {
        return;
      }
      Field delegateField =
          Class.forName("io.opentelemetry.api.GlobalOpenTelemetry$ObfuscatedOpenTelemetry")
              .getDeclaredField("delegate");
      delegateField.setAccessible(true);
      Object delegate = delegateField.get(globalOpenTelemetry);
      if (delegate instanceof OpenTelemetrySdk) {
        OpenTelemetrySdk sdk = ((OpenTelemetrySdk) delegate);
        sdk.getSdkTracerProvider().shutdown().join(10, TimeUnit.SECONDS);
        sdk.getSdkMeterProvider().shutdown().join(10, TimeUnit.SECONDS);
        sdk.getSdkLoggerProvider().shutdown().join(10, TimeUnit.SECONDS);
      }
    } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
      throw new IllegalStateException("Error shutting down global SDK.", e);
    }
  }
}
