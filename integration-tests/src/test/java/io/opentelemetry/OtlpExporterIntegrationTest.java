/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.Testcontainers.exposeHostPorts;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span.Link;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration test to verify OTLP gRPC and HTTP exporters work. Verifies integration with the
 * OpenTelemetry Collector. The Collector is configured to accept OTLP data over gRPC and HTTP and
 * to export data over gRPC to a server running in process, allowing assertions to be made against
 * the data.
 */
@Testcontainers(disabledWithoutDocker = true)
class OtlpExporterIntegrationTest {

  private static final String COLLECTOR_IMAGE =
      "ghcr.io/open-telemetry/opentelemetry-java/otel-collector";
  private static final Integer COLLECTOR_OTLP_GRPC_PORT = 4317;
  private static final Integer COLLECTOR_OTLP_HTTP_PORT = 4318;
  private static final Integer COLLECTOR_HEALTH_CHECK_PORT = 13133;
  private static final Resource RESOURCE =
      Resource.getDefault().toBuilder()
          .put(ResourceAttributes.SERVICE_NAME, "integration test")
          .build();
  private static final Logger LOGGER =
      Logger.getLogger(OtlpExporterIntegrationTest.class.getName());

  private static OtlpGrpcServer grpcServer;
  private static GenericContainer<?> collector;

  @BeforeAll
  static void beforeAll() {
    grpcServer = new OtlpGrpcServer();
    grpcServer.start();

    // Expose the port the in-process OTLP gRPC server will run on before the collector is
    // initialized so the collector can connect to it.
    exposeHostPorts(grpcServer.httpPort());

    collector =
        new GenericContainer<>(DockerImageName.parse(COLLECTOR_IMAGE))
            .withEnv("LOGGING_EXPORTER_LOG_LEVEL", "INFO")
            .withEnv(
                "OTLP_EXPORTER_ENDPOINT", "host.testcontainers.internal:" + grpcServer.httpPort())
            .withClasspathResourceMapping(
                "otel-config.yaml", "/otel-config.yaml", BindMode.READ_ONLY)
            .withCommand("--config", "/otel-config.yaml")
            .withLogConsumer(
                outputFrame ->
                    LOGGER.log(Level.INFO, outputFrame.getUtf8String().replace("\n", "")))
            .withExposedPorts(
                COLLECTOR_OTLP_GRPC_PORT, COLLECTOR_OTLP_HTTP_PORT, COLLECTOR_HEALTH_CHECK_PORT)
            .waitingFor(Wait.forHttp("/").forPort(COLLECTOR_HEALTH_CHECK_PORT));
    collector.start();
  }

  @AfterAll
  static void afterAll() {
    grpcServer.stop().join();
    collector.stop();
  }

  @BeforeEach
  void beforeEach() {
    grpcServer.reset();
    GlobalOpenTelemetry.resetForTest();
  }

  @AfterEach
  void afterEach() {}

  @Test
  void testOtlpGrpcTraceExport() {
    SpanExporter otlpGrpcTraceExporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_GRPC_PORT))
            .setCompression("gzip")
            .build();

    testTraceExport(otlpGrpcTraceExporter);
  }

  @Test
  void testOtlpHttpTraceExport() {
    SpanExporter otlpGrpcTraceExporter =
        OtlpHttpSpanExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_HTTP_PORT)
                    + "/v1/traces")
            .setCompression("gzip")
            .build();

    testTraceExport(otlpGrpcTraceExporter);
  }

  private static void testTraceExport(SpanExporter spanExporter) {
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .setResource(RESOURCE)
            .build();

    SpanContext linkContext =
        SpanContext.create(
            IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(),
            TraceFlags.getDefault(),
            TraceState.getDefault());
    Span span =
        tracerProvider
            .get(OtlpExporterIntegrationTest.class.getName())
            .spanBuilder("my span name")
            .addLink(linkContext)
            .startSpan();
    span.setAttribute("key", "value");
    span.addEvent("event");
    span.end();

    Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .until(() -> grpcServer.traceRequests.size() == 1);

    ExportTraceServiceRequest request = grpcServer.traceRequests.get(0);
    assertThat(request.getResourceSpansCount()).isEqualTo(1);

    ResourceSpans resourceSpans = request.getResourceSpans(0);
    assertThat(resourceSpans.getResource().getAttributesList())
        .contains(
            KeyValue.newBuilder()
                .setKey(ResourceAttributes.SERVICE_NAME.getKey())
                .setValue(AnyValue.newBuilder().setStringValue("integration test").build())
                .build());
    assertThat(resourceSpans.getInstrumentationLibrarySpansCount()).isEqualTo(1);

    InstrumentationLibrarySpans ilSpans = resourceSpans.getInstrumentationLibrarySpans(0);
    assertThat(ilSpans.getInstrumentationLibrary().getName())
        .isEqualTo(OtlpExporterIntegrationTest.class.getName());
    assertThat(ilSpans.getSpansCount()).isEqualTo(1);

    io.opentelemetry.proto.trace.v1.Span protoSpan = ilSpans.getSpans(0);
    assertThat(protoSpan.getTraceId().toByteArray())
        .isEqualTo(span.getSpanContext().getTraceIdBytes());
    assertThat(protoSpan.getSpanId().toByteArray())
        .isEqualTo(span.getSpanContext().getSpanIdBytes());
    assertThat(protoSpan.getName()).isEqualTo("my span name");
    assertThat(protoSpan.getAttributesList())
        .isEqualTo(
            Collections.singletonList(
                KeyValue.newBuilder()
                    .setKey("key")
                    .setValue(AnyValue.newBuilder().setStringValue("value").build())
                    .build()));
    assertThat(protoSpan.getEventsCount()).isEqualTo(1);
    assertThat(protoSpan.getEvents(0).getName()).isEqualTo("event");
    assertThat(protoSpan.getLinksCount()).isEqualTo(1);
    Link link = protoSpan.getLinks(0);
    assertThat(link.getTraceId().toByteArray()).isEqualTo(linkContext.getTraceIdBytes());
    assertThat(link.getSpanId().toByteArray()).isEqualTo(linkContext.getSpanIdBytes());
  }

  @Test
  void testOtlpGrpcMetricExport() {
    MetricExporter otlpGrpcMetricExporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_GRPC_PORT))
            .setCompression("gzip")
            .build();

    testMetricExport(otlpGrpcMetricExporter);
  }

  @Test
  void testOtlpHttpMetricExport() {
    MetricExporter otlpGrpcMetricExporter =
        OtlpHttpMetricExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_HTTP_PORT)
                    + "/v1/metrics")
            .setCompression("gzip")
            .build();

    testMetricExport(otlpGrpcMetricExporter);
  }

  private static void testMetricExport(MetricExporter metricExporter) {
    PeriodicMetricReader.Factory reader =
        new PeriodicMetricReader.Factory(metricExporter, Duration.ofSeconds(5));
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().setResource(RESOURCE).register(reader).build();

    Meter meter = meterProvider.meterBuilder(OtlpExporterIntegrationTest.class.getName()).build();

    LongCounter longCounter = meter.counterBuilder("my-counter").build();
    longCounter.add(100, Attributes.builder().put("key", "value").build());

    try {
      Awaitility.await()
          .atMost(Duration.ofSeconds(30))
          .until(() -> grpcServer.metricRequests.size() == 1);
    } finally {
      meterProvider.shutdown();
    }

    ExportMetricsServiceRequest request = grpcServer.metricRequests.get(0);
    assertThat(request.getResourceMetricsCount()).isEqualTo(1);

    ResourceMetrics resourceMetrics = request.getResourceMetrics(0);
    assertThat(resourceMetrics.getResource().getAttributesList())
        .contains(
            KeyValue.newBuilder()
                .setKey(ResourceAttributes.SERVICE_NAME.getKey())
                .setValue(AnyValue.newBuilder().setStringValue("integration test").build())
                .build());
    assertThat(resourceMetrics.getInstrumentationLibraryMetricsCount()).isEqualTo(1);

    InstrumentationLibraryMetrics ilMetrics = resourceMetrics.getInstrumentationLibraryMetrics(0);
    assertThat(ilMetrics.getInstrumentationLibrary().getName())
        .isEqualTo(OtlpExporterIntegrationTest.class.getName());
    assertThat(ilMetrics.getMetricsCount()).isEqualTo(1);

    Metric metric = ilMetrics.getMetrics(0);
    assertThat(metric.getName()).isEqualTo("my-counter");
    assertThat(metric.getDataCase()).isEqualTo(Metric.DataCase.SUM);

    Sum sum = metric.getSum();
    assertThat(sum.getAggregationTemporality())
        .isEqualTo(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE);
    assertThat(sum.getDataPointsCount()).isEqualTo(1);

    NumberDataPoint dataPoint = sum.getDataPoints(0);
    assertThat(dataPoint.getAsInt()).isEqualTo(100);
    assertThat(dataPoint.getAttributesList())
        .isEqualTo(
            Collections.singletonList(
                KeyValue.newBuilder()
                    .setKey("key")
                    .setValue(AnyValue.newBuilder().setStringValue("value").build())
                    .build()));
  }

  private static class OtlpGrpcServer extends ServerExtension {

    private final List<ExportTraceServiceRequest> traceRequests = new ArrayList<>();
    private final List<ExportMetricsServiceRequest> metricRequests = new ArrayList<>();

    private void reset() {
      traceRequests.clear();
      metricRequests.clear();
    }

    @Override
    protected void configure(ServerBuilder sb) {
      sb.service(
          GrpcService.builder()
              .addService(
                  new TraceServiceGrpc.TraceServiceImplBase() {
                    @Override
                    public void export(
                        ExportTraceServiceRequest request,
                        StreamObserver<ExportTraceServiceResponse> responseObserver) {
                      traceRequests.add(request);
                      responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
                      responseObserver.onCompleted();
                    }
                  })
              .addService(
                  new MetricsServiceGrpc.MetricsServiceImplBase() {
                    @Override
                    public void export(
                        ExportMetricsServiceRequest request,
                        StreamObserver<ExportMetricsServiceResponse> responseObserver) {
                      metricRequests.add(request);
                      responseObserver.onNext(ExportMetricsServiceResponse.getDefaultInstance());
                      responseObserver.onCompleted();
                    }
                  })
              .build());
      sb.http(0);
    }
  }
}
