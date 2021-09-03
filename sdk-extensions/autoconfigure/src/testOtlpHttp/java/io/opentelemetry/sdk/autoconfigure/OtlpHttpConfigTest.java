/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.tls.HeldCertificate;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpHttpConfigTest {

  private static final BlockingQueue<ExportTraceServiceRequest> traceRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<ExportMetricsServiceRequest> metricRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<RequestHeaders> requestHeaders = new LinkedBlockingDeque<>();

  @RegisterExtension
  @Order(1)
  public static final CertificateExtension certificateExtension = new CertificateExtension();

  private static class CertificateExtension implements BeforeAllCallback {
    private HeldCertificate heldCertificate;
    private String filePath;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
      heldCertificate =
          new HeldCertificate.Builder()
              .commonName("localhost")
              .addSubjectAlternativeName(InetAddress.getByName("localhost").getCanonicalHostName())
              .build();
      Path file = Files.createTempFile("test-cert", ".pem");
      Files.write(file, heldCertificate.certificatePem().getBytes(StandardCharsets.UTF_8));
      filePath = file.toAbsolutePath().toString();
    }
  }

  @RegisterExtension
  @Order(2)
  public static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
                  "/v1/traces",
                  httpService(traceRequests, ExportTraceServiceRequest.getDefaultInstance()))
              .service(
                  "/v1/metrics",
                  httpService(metricRequests, ExportMetricsServiceRequest.getDefaultInstance()));
          sb.tls(
              certificateExtension.heldCertificate.keyPair().getPrivate(),
              certificateExtension.heldCertificate.certificate());
        }
      };

  @SuppressWarnings("unchecked")
  private static <T extends Message> HttpService httpService(
      BlockingQueue<T> queue, T defaultMessage) {
    return (ctx, req) ->
        HttpResponse.from(
            req.aggregate()
                .thenApply(
                    aggReq -> {
                      requestHeaders.add(aggReq.headers());
                      try {
                        queue.add(
                            (T)
                                defaultMessage
                                    .getParserForType()
                                    .parseFrom(aggReq.content().array()));
                      } catch (InvalidProtocolBufferException e) {
                        return HttpResponse.of(HttpStatus.BAD_REQUEST);
                      }
                      return HttpResponse.of(HttpStatus.OK);
                    }));
  }

  @BeforeEach
  void setUp() {
    traceRequests.clear();
    metricRequests.clear();
    requestHeaders.clear();
    GlobalOpenTelemetry.resetForTest();
    IntervalMetricReader.resetGlobalForTest();
  }

  @AfterEach
  public void tearDown() {
    GlobalOpenTelemetry.resetForTest();
    IntervalMetricReader.resetGlobalForTest();
  }

  @Test
  void configureExportersGeneral() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.experimental.exporter.otlp.protocol", "http/protobuf");
    props.put("otel.exporter.otlp.traces.endpoint", traceEndpoint());
    props.put("otel.exporter.otlp.metrics.endpoint", metricEndpoint());
    props.put("otel.exporter.otlp.certificate", certificateExtension.filePath);
    props.put("otel.exporter.otlp.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.timeout", "15s");
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter("otlp", properties, Collections.emptyMap());
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            properties, SdkMeterProvider.builder().build());

    assertThat(spanExporter)
        .extracting("client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            spanExporter
                .export(Lists.newArrayList(generateFakeSpan()))
                .join(15, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(traceRequests).hasSize(1);
    assertThat(requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/traces")
                    && headers.contains("header-key", "header-value"));

    assertThat(metricExporter)
        .extracting("client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            metricExporter
                .export(Lists.newArrayList(generateFakeMetric()))
                .join(15, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(metricRequests).hasSize(1);
    assertThat(requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/metrics")
                    && headers.contains("header-key", "header-value"));
  }

  @Test
  void configureSpanExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.experimental.exporter.otlp.protocol", "grpc");
    props.put("otel.experimental.exporter.otlp.traces.protocol", "http/protobuf");
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.traces.endpoint", traceEndpoint());
    props.put("otel.exporter.otlp.traces.certificate", certificateExtension.filePath);
    props.put("otel.exporter.otlp.traces.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.traces.timeout", "15s");
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp", DefaultConfigProperties.createForTest(props), Collections.emptyMap());

    assertThat(spanExporter)
        .extracting("client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            spanExporter
                .export(Lists.newArrayList(generateFakeSpan()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(traceRequests).hasSize(1);
    assertThat(requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/traces")
                    && headers.contains("header-key", "header-value"));
  }

  @Test
  public void configureMetricExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.experimental.exporter.otlp.protocol", "grpc");
    props.put("otel.experimental.exporter.otlp.metrics.protocol", "http/protobuf");
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.metrics.endpoint", metricEndpoint());
    props.put("otel.exporter.otlp.metrics.certificate", certificateExtension.filePath);
    props.put("otel.exporter.otlp.metrics.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.metrics.timeout", "15s");
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            DefaultConfigProperties.createForTest(props), SdkMeterProvider.builder().build());

    assertThat(metricExporter)
        .extracting("client", as(InstanceOfAssertFactories.type(OkHttpClient.class)))
        .extracting(OkHttpClient::callTimeoutMillis)
        .isEqualTo((int) TimeUnit.SECONDS.toMillis(15));
    assertThat(
            metricExporter
                .export(Lists.newArrayList(generateFakeMetric()))
                .join(15, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(metricRequests).hasSize(1);
    assertThat(requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(":path", "/v1/metrics")
                    && headers.contains("header-key", "header-value"));
  }

  @Test
  void configureTlsInvalidCertificatePath() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.experimental.exporter.otlp.protocol", "http/protobuf");
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
                    properties, SdkMeterProvider.builder().build()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Invalid OTLP certificate path:");
  }

  private static SpanData generateFakeSpan() {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setName("name")
        .setStartEpochNanos(MILLISECONDS.toNanos(System.currentTimeMillis()))
        .setEndEpochNanos(MILLISECONDS.toNanos(System.currentTimeMillis()))
        .setKind(SpanKind.SERVER)
        .setStatus(StatusData.error())
        .setTotalRecordedEvents(0)
        .setTotalRecordedLinks(0)
        .build();
  }

  private static MetricData generateFakeMetric() {
    return MetricData.createLongSum(
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
                    10))));
  }

  @Test
  void configuresGlobal() {
    System.setProperty("otel.experimental.exporter.otlp.protocol", "http/protobuf");
    System.setProperty("otel.exporter.otlp.traces.endpoint", traceEndpoint());
    System.setProperty("otel.exporter.otlp.metrics.endpoint", metricEndpoint());
    System.setProperty("otel.exporter.otlp.certificate", certificateExtension.filePath);
    System.setProperty("otel.imr.export.interval", "1s");

    GlobalOpenTelemetry.get().getTracer("test").spanBuilder("test").startSpan().end();

    await()
        .untilAsserted(
            () -> {
              assertThat(traceRequests).hasSize(1);

              // Not well defined how many metric exports would have happened by now, check that
              // any did. Metrics are recorded by OtlpHttpSpanExporter, BatchSpanProcessor, and
              // potentially others.
              assertThat(metricRequests).isNotEmpty();
            });
  }

  private static String traceEndpoint() {
    return String.format("https://localhost:%s/v1/traces", server.httpsPort());
  }

  private static String metricEndpoint() {
    return String.format("https://localhost:%s/v1/metrics", server.httpsPort());
  }
}
