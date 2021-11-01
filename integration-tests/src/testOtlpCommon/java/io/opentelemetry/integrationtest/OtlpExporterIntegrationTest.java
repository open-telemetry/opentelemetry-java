/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.testcontainers.Testcontainers.exposeHostPorts;

import com.google.protobuf.InvalidProtocolBufferException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span.Link;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
abstract class OtlpExporterIntegrationTest {

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

  @ParameterizedTest
  @ValueSource(strings = {"gzip", "none"})
  void testOtlpGrpcTraceExport(String compression) {
    SpanExporter otlpGrpcTraceExporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_GRPC_PORT))
            .setCompression(compression)
            .build();

    testTraceExport(otlpGrpcTraceExporter);
  }

  @ParameterizedTest
  @ValueSource(strings = {"gzip", "none"})
  void testOtlpHttpTraceExport(String compression) {
    SpanExporter otlpGrpcTraceExporter =
        OtlpHttpSpanExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_HTTP_PORT)
                    + "/v1/traces")
            .setCompression(compression)
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

    await()
        .atMost(Duration.ofSeconds(30))
        .untilAsserted(() -> assertThat(grpcServer.traceRequests).hasSize(1));

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

  @ParameterizedTest
  @ValueSource(strings = {"gzip", "none"})
  void testOtlpGrpcMetricExport(String compression) {
    MetricExporter otlpGrpcMetricExporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_GRPC_PORT))
            .setCompression(compression)
            .build();

    testMetricExport(otlpGrpcMetricExporter);
  }

  @ParameterizedTest
  @ValueSource(strings = {"gzip", "none"})
  void testOtlpHttpMetricExport(String compression) {
    MetricExporter otlpGrpcMetricExporter =
        OtlpHttpMetricExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_HTTP_PORT)
                    + "/v1/metrics")
            .setCompression(compression)
            .build();

    testMetricExport(otlpGrpcMetricExporter);
  }

  private static void testMetricExport(MetricExporter metricExporter) {
    MetricReaderFactory reader =
        PeriodicMetricReader.builder(metricExporter)
            .setInterval(Duration.ofSeconds(5))
            .newMetricReaderFactory();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().setResource(RESOURCE).registerMetricReader(reader).build();

    Meter meter = meterProvider.meterBuilder(OtlpExporterIntegrationTest.class.getName()).build();

    LongCounter longCounter = meter.counterBuilder("my-counter").build();
    longCounter.add(100, Attributes.builder().put("key", "value").build());

    try {
      await()
          .atMost(Duration.ofSeconds(30))
          .untilAsserted(() -> assertThat(grpcServer.metricRequests).hasSize(1));
    } finally {
      meterProvider.close();
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

  @ParameterizedTest
  @ValueSource(strings = {"gzip", "none"})
  void testOtlpGrpcLogExport(String compression) {
    LogExporter otlpGrpcLogExporter =
        OtlpGrpcLogExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_GRPC_PORT))
            .setCompression(compression)
            .build();

    testLogExporter(otlpGrpcLogExporter);
  }

  @ParameterizedTest
  @ValueSource(strings = {"gzip", "none"})
  void testOtlpHttpLogExport(String compression) {
    LogExporter otlpHttpLogExporter =
        OtlpHttpLogExporter.builder()
            .setEndpoint(
                "http://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_HTTP_PORT)
                    + "/v1/logs")
            .setCompression(compression)
            .build();

    testLogExporter(otlpHttpLogExporter);
  }

  private static void testLogExporter(LogExporter logExporter) {
    LogData logData =
        LogData.builder(
                RESOURCE,
                InstrumentationLibraryInfo.create(
                    OtlpExporterIntegrationTest.class.getName(), null))
            .setName("log-name")
            .setBody(Body.stringBody("log body"))
            .setAttributes(Attributes.builder().put("key", "value").build())
            .setSeverity(Severity.DEBUG)
            .setSeverityText("DEBUG")
            .setTraceId(IdGenerator.random().generateTraceId())
            .setSpanId(IdGenerator.random().generateSpanId())
            .setEpoch(Instant.now())
            .setFlags(0)
            .build();

    logExporter.export(Collections.singletonList(logData));

    await()
        .atMost(Duration.ofSeconds(30))
        .untilAsserted(() -> assertThat(grpcServer.logRequests).hasSize(1));

    ExportLogsServiceRequest request = grpcServer.logRequests.get(0);
    assertThat(request.getResourceLogsCount()).isEqualTo(1);

    ResourceLogs resourceLogs = request.getResourceLogs(0);
    assertThat(resourceLogs.getResource().getAttributesList())
        .contains(
            KeyValue.newBuilder()
                .setKey(ResourceAttributes.SERVICE_NAME.getKey())
                .setValue(AnyValue.newBuilder().setStringValue("integration test").build())
                .build());
    assertThat(resourceLogs.getInstrumentationLibraryLogsCount()).isEqualTo(1);

    InstrumentationLibraryLogs ilLogs = resourceLogs.getInstrumentationLibraryLogs(0);
    assertThat(ilLogs.getInstrumentationLibrary().getName())
        .isEqualTo(OtlpExporterIntegrationTest.class.getName());
    assertThat(ilLogs.getLogsCount()).isEqualTo(1);

    io.opentelemetry.proto.logs.v1.LogRecord protoLog = ilLogs.getLogs(0);
    assertThat(protoLog.getName()).isEqualTo("log-name");
    assertThat(protoLog.getBody().getStringValue()).isEqualTo("log body");
    assertThat(protoLog.getAttributesList())
        .isEqualTo(
            Collections.singletonList(
                KeyValue.newBuilder()
                    .setKey("key")
                    .setValue(AnyValue.newBuilder().setStringValue("value").build())
                    .build()));
    assertThat(protoLog.getSeverityNumber().getNumber())
        .isEqualTo(logData.getSeverity().getSeverityNumber());
    assertThat(protoLog.getSeverityText()).isEqualTo("DEBUG");
    assertThat(TraceId.fromBytes(protoLog.getTraceId().toByteArray()))
        .isEqualTo(logData.getTraceId());
    assertThat(SpanId.fromBytes(protoLog.getSpanId().toByteArray())).isEqualTo(logData.getSpanId());
    assertThat(protoLog.getTimeUnixNano()).isEqualTo(logData.getEpochNanos());
    assertThat(protoLog.getFlags()).isEqualTo(logData.getFlags());
  }

  private static class OtlpGrpcServer extends ServerExtension {

    private final List<ExportTraceServiceRequest> traceRequests = new ArrayList<>();
    private final List<ExportMetricsServiceRequest> metricRequests = new ArrayList<>();
    private final List<ExportLogsServiceRequest> logRequests = new ArrayList<>();

    private void reset() {
      traceRequests.clear();
      metricRequests.clear();
      logRequests.clear();
    }

    @Override
    protected void configure(ServerBuilder sb) {
      sb.service(
          "/opentelemetry.proto.collector.trace.v1.TraceService/Export",
          new AbstractUnaryGrpcService() {
            @Override
            protected CompletionStage<byte[]> handleMessage(
                ServiceRequestContext ctx, byte[] message) {
              try {
                traceRequests.add(ExportTraceServiceRequest.parseFrom(message));
              } catch (InvalidProtocolBufferException e) {
                throw new UncheckedIOException(e);
              }
              return completedFuture(ExportTraceServiceResponse.getDefaultInstance().toByteArray());
            }
          });
      sb.service(
          "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export",
          new AbstractUnaryGrpcService() {
            @Override
            protected CompletionStage<byte[]> handleMessage(
                ServiceRequestContext ctx, byte[] message) {
              try {
                metricRequests.add(ExportMetricsServiceRequest.parseFrom(message));
              } catch (InvalidProtocolBufferException e) {
                throw new UncheckedIOException(e);
              }
              return completedFuture(
                  ExportMetricsServiceResponse.getDefaultInstance().toByteArray());
            }
          });
      sb.service(
          "/opentelemetry.proto.collector.logs.v1.LogsService/Export",
          new AbstractUnaryGrpcService() {
            @Override
            protected CompletionStage<byte[]> handleMessage(
                ServiceRequestContext ctx, byte[] message) {
              try {
                logRequests.add(ExportLogsServiceRequest.parseFrom(message));
              } catch (InvalidProtocolBufferException e) {
                throw new UncheckedIOException(e);
              }
              return completedFuture(ExportLogsServiceResponse.getDefaultInstance().toByteArray());
            }
          });
      sb.http(0);
    }
  }
}
