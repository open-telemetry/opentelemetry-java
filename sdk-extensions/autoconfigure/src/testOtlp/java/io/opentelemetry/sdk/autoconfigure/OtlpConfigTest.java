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
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpConfigTest {

  private static final BlockingQueue<ExportTraceServiceRequest> otlpTraceRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<ExportMetricsServiceRequest> otlpMetricsRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<RequestHeaders> requestHeaders = new LinkedBlockingDeque<>();

  @RegisterExtension
  @Order(1)
  public static final SelfSignedCertificateExtension certificate =
      new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(2)
  public static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              GrpcService.builder()
                  // OTLP spans
                  .addService(
                      new TraceServiceGrpc.TraceServiceImplBase() {
                        @Override
                        public void export(
                            ExportTraceServiceRequest request,
                            StreamObserver<ExportTraceServiceResponse> responseObserver) {
                          otlpTraceRequests.add(request);
                          responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
                          responseObserver.onCompleted();
                        }
                      })
                  // OTLP metrics
                  .addService(
                      new MetricsServiceGrpc.MetricsServiceImplBase() {
                        @Override
                        public void export(
                            ExportMetricsServiceRequest request,
                            StreamObserver<ExportMetricsServiceResponse> responseObserver) {
                          if (request.getResourceMetricsCount() > 0) {
                            otlpMetricsRequests.add(request);
                          }
                          responseObserver.onNext(
                              ExportMetricsServiceResponse.getDefaultInstance());
                          responseObserver.onCompleted();
                        }
                      })
                  .useBlockingTaskExecutor(true)
                  .build());
          sb.decorator(
              (delegate, ctx, req) -> {
                requestHeaders.add(req.headers());
                return delegate.serve(ctx, req);
              });
          sb.tls(certificate.certificateFile(), certificate.privateKeyFile());
        }
      };

  @BeforeEach
  void setUp() {
    otlpTraceRequests.clear();
    otlpMetricsRequests.clear();
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
    props.put("otel.exporter.otlp.endpoint", "https://localhost:" + server.httpsPort());
    props.put("otel.exporter.otlp.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.exporter.otlp.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.timeout", "5s");
    ConfigProperties properties = ConfigProperties.createForTest(props);
    SpanExporter spanExporter = SpanExporterConfiguration.configureExporter("otlp", properties);
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            properties, SdkMeterProvider.builder().build());

    assertThat(spanExporter).extracting("timeoutNanos").isEqualTo(TimeUnit.SECONDS.toNanos(5));
    assertThat(
            spanExporter
                .export(Lists.newArrayList(generateFakeSpan()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(otlpTraceRequests).hasSize(1);
    assertThat(requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(
                        ":path", "/opentelemetry.proto.collector.trace.v1.TraceService/Export")
                    && headers.contains("header-key", "header-value"));

    assertThat(metricExporter).extracting("timeoutNanos").isEqualTo(TimeUnit.SECONDS.toNanos(5));
    assertThat(
            metricExporter
                .export(Lists.newArrayList(generateFakeMetric()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(otlpMetricsRequests).hasSize(1);
    assertThat(requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(
                        ":path", "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export")
                    && headers.contains("header-key", "header-value"));
  }

  @Test
  void configureSpanExporter() {
    // Set values for general and signal specific properties. Signal specific should override
    // general.
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.endpoint", "http://foo.bar");
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    props.put("otel.exporter.otlp.headers", "header-key=dummy-value");
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.traces.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.traces.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.exporter.otlp.traces.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.traces.timeout", "5s");
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter("otlp", ConfigProperties.createForTest(props));

    assertThat(spanExporter).extracting("timeoutNanos").isEqualTo(TimeUnit.SECONDS.toNanos(5));
    assertThat(
            spanExporter
                .export(Lists.newArrayList(generateFakeSpan()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(otlpTraceRequests).hasSize(1);
    assertThat(requestHeaders)
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
    props.put("otel.exporter.otlp.timeout", "10s");
    props.put("otel.exporter.otlp.metrics.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.metrics.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.exporter.otlp.metrics.headers", "header-key=header-value");
    props.put("otel.exporter.otlp.metrics.timeout", "5s");
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            ConfigProperties.createForTest(props), SdkMeterProvider.builder().build());

    assertThat(metricExporter).extracting("timeoutNanos").isEqualTo(TimeUnit.SECONDS.toNanos(5));
    assertThat(
            metricExporter
                .export(Lists.newArrayList(generateFakeMetric()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();
    assertThat(otlpMetricsRequests).hasSize(1);
    assertThat(requestHeaders)
        .anyMatch(
            headers ->
                headers.contains(
                        ":path", "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export")
                    && headers.contains("header-key", "header-value"));
  }

  @Test
  void configureTlsInvalidCertificatePath() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.certificate", Paths.get("foo", "bar", "baz").toString());
    ConfigProperties properties = ConfigProperties.createForTest(props);

    assertThatThrownBy(() -> SpanExporterConfiguration.configureExporter("otlp", properties))
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
    System.setProperty("otel.exporter.otlp.endpoint", "https://localhost:" + server.httpsPort());
    System.setProperty(
        "otel.exporter.otlp.certificate", certificate.certificateFile().getAbsolutePath());
    System.setProperty("otel.imr.export.interval", "1s");

    GlobalOpenTelemetry.get().getTracer("test").spanBuilder("test").startSpan().end();

    await()
        .untilAsserted(
            () -> {
              assertThat(otlpTraceRequests).hasSize(1);

              // Not well defined how many metric exports would have happened by now, check that
              // any did. Metrics are recorded by OtlpGrpcSpanExporter, BatchSpanProcessor, and
              // potentially others.
              assertThat(otlpMetricsRequests).isNotEmpty();
            });
  }
}
