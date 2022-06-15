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
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
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
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
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
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span.Link;
import io.opentelemetry.sdk.logs.LogEmitter;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

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
  private static final Integer COLLECTOR_OTLP_GRPC_MTLS_PORT = 5317;
  private static final Integer COLLECTOR_OTLP_HTTP_MTLS_PORT = 5318;
  private static final Integer COLLECTOR_HEALTH_CHECK_PORT = 13133;
  private static final Resource RESOURCE =
      Resource.getDefault().toBuilder()
          .put(ResourceAttributes.SERVICE_NAME, "integration test")
          .build();

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

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
            .withCopyFileToContainer(
                MountableFile.forHostPath(serverTls.certificateFile().toPath(), 0555),
                "/server.cert")
            .withCopyFileToContainer(
                MountableFile.forHostPath(serverTls.privateKeyFile().toPath(), 0555), "/server.key")
            .withCopyFileToContainer(
                MountableFile.forHostPath(clientTls.certificateFile().toPath(), 0555),
                "/client.cert")
            .withEnv(
                "OTLP_EXPORTER_ENDPOINT", "host.testcontainers.internal:" + grpcServer.httpPort())
            .withEnv("MTLS_CLIENT_CERTIFICATE", "/client.cert")
            .withEnv("MTLS_SERVER_CERTIFICATE", "/server.cert")
            .withEnv("MTLS_SERVER_KEY", "/server.key")
            .withClasspathResourceMapping(
                "otel-config.yaml", "/otel-config.yaml", BindMode.READ_ONLY)
            .withCommand("--config", "/otel-config.yaml")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("otel-collector")))
            .withExposedPorts(
                COLLECTOR_OTLP_GRPC_PORT,
                COLLECTOR_OTLP_HTTP_PORT,
                COLLECTOR_OTLP_GRPC_MTLS_PORT,
                COLLECTOR_OTLP_HTTP_MTLS_PORT,
                COLLECTOR_HEALTH_CHECK_PORT)
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

  @Test
  void testOtlpGrpcTraceExport_mtls() throws Exception {
    SpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint(
                "https://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_GRPC_MTLS_PORT))
            .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded())
            .setTrustedCertificates(serverTls.certificate().getEncoded())
            .build();

    testTraceExport(exporter);
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

  @Test
  void testOtlpHttpTraceExport_mtls() throws Exception {
    SpanExporter exporter =
        OtlpHttpSpanExporter.builder()
            .setEndpoint(
                "https://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_HTTP_MTLS_PORT)
                    + "/v1/traces")
            .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded())
            .setTrustedCertificates(serverTls.certificate().getEncoded())
            .build();

    testTraceExport(exporter);
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

    // Closing triggers flush of processor
    tracerProvider.close();

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
    assertThat(resourceSpans.getScopeSpansCount()).isEqualTo(1);

    ScopeSpans ilSpans = resourceSpans.getScopeSpans(0);
    assertThat(ilSpans.getScope().getName()).isEqualTo(OtlpExporterIntegrationTest.class.getName());
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

  @Test
  void testOtlpGrpcMetricExport_mtls() throws Exception {
    MetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint(
                "https://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_GRPC_MTLS_PORT))
            .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded())
            .setTrustedCertificates(serverTls.certificate().getEncoded())
            .build();

    testMetricExport(exporter);
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

  @Test
  void testOtlpHttpMetricExport_mtls() throws Exception {
    MetricExporter exporter =
        OtlpHttpMetricExporter.builder()
            .setEndpoint(
                "https://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_HTTP_MTLS_PORT)
                    + "/v1/metrics")
            .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded())
            .setTrustedCertificates(serverTls.certificate().getEncoded())
            .build();

    testMetricExport(exporter);
  }

  private static void testMetricExport(MetricExporter metricExporter) {
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .setResource(RESOURCE)
            .registerMetricReader(
                PeriodicMetricReader.builder(metricExporter)
                    .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
                    .build())
            .build();

    Meter meter = meterProvider.meterBuilder(OtlpExporterIntegrationTest.class.getName()).build();

    LongCounter longCounter = meter.counterBuilder("my-counter").build();
    longCounter.add(100, Attributes.builder().put("key", "value").build());

    // Closing triggers flush of reader
    meterProvider.close();

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> assertThat(grpcServer.metricRequests).hasSize(1));

    ExportMetricsServiceRequest request = grpcServer.metricRequests.get(0);
    assertThat(request.getResourceMetricsCount()).isEqualTo(1);

    ResourceMetrics resourceMetrics = request.getResourceMetrics(0);
    assertThat(resourceMetrics.getResource().getAttributesList())
        .contains(
            KeyValue.newBuilder()
                .setKey(ResourceAttributes.SERVICE_NAME.getKey())
                .setValue(AnyValue.newBuilder().setStringValue("integration test").build())
                .build());
    assertThat(resourceMetrics.getScopeMetricsCount()).isEqualTo(1);

    ScopeMetrics ilMetrics = resourceMetrics.getScopeMetrics(0);
    assertThat(ilMetrics.getScope().getName())
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

  @Test
  void testOtlpGrpcLogExport_mtls() throws Exception {
    LogExporter exporter =
        OtlpGrpcLogExporter.builder()
            .setEndpoint(
                "https://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_GRPC_MTLS_PORT))
            .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded())
            .setTrustedCertificates(serverTls.certificate().getEncoded())
            .build();

    testLogExporter(exporter);
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

  @Test
  void testOtlpHttpLogExport_mtls() throws Exception {
    LogExporter exporter =
        OtlpHttpLogExporter.builder()
            .setEndpoint(
                "https://"
                    + collector.getHost()
                    + ":"
                    + collector.getMappedPort(COLLECTOR_OTLP_HTTP_MTLS_PORT)
                    + "/v1/logs")
            .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded())
            .setTrustedCertificates(serverTls.certificate().getEncoded())
            .build();

    testLogExporter(exporter);
  }

  private static void testLogExporter(LogExporter logExporter) {
    SdkLogEmitterProvider logEmitterProvider =
        SdkLogEmitterProvider.builder()
            .setResource(RESOURCE)
            .addLogProcessor(SimpleLogProcessor.create(logExporter))
            .build();

    LogEmitter logEmitter = logEmitterProvider.get(OtlpExporterIntegrationTest.class.getName());

    SpanContext spanContext =
        SpanContext.create(
            IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(),
            TraceFlags.getDefault(),
            TraceState.getDefault());

    try (Scope unused = Span.wrap(spanContext).makeCurrent()) {
      logEmitter
          .logBuilder()
          .setBody("log body")
          .setAttributes(Attributes.builder().put("key", "value").build())
          .setSeverity(Severity.DEBUG)
          .setSeverityText("DEBUG")
          .setEpoch(Instant.now())
          .setContext(Context.current())
          .emit();
    }

    // Closing triggers flush of processor
    logEmitterProvider.close();

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
    assertThat(resourceLogs.getScopeLogsCount()).isEqualTo(1);

    ScopeLogs ilLogs = resourceLogs.getScopeLogs(0);
    assertThat(ilLogs.getScope().getName()).isEqualTo(OtlpExporterIntegrationTest.class.getName());
    assertThat(ilLogs.getLogRecordsCount()).isEqualTo(1);

    io.opentelemetry.proto.logs.v1.LogRecord protoLog = ilLogs.getLogRecords(0);
    assertThat(protoLog.getBody().getStringValue()).isEqualTo("log body");
    assertThat(protoLog.getAttributesList())
        .isEqualTo(
            Collections.singletonList(
                KeyValue.newBuilder()
                    .setKey("key")
                    .setValue(AnyValue.newBuilder().setStringValue("value").build())
                    .build()));
    assertThat(protoLog.getSeverityNumber().getNumber())
        .isEqualTo(Severity.DEBUG.getSeverityNumber());
    assertThat(protoLog.getSeverityText()).isEqualTo("DEBUG");
    assertThat(TraceId.fromBytes(protoLog.getTraceId().toByteArray()))
        .isEqualTo(spanContext.getTraceId());
    assertThat(SpanId.fromBytes(protoLog.getSpanId().toByteArray()))
        .isEqualTo(spanContext.getSpanId());
    assertThat(TraceFlags.fromByte((byte) protoLog.getFlags()))
        .isEqualTo(spanContext.getTraceFlags());
    assertThat(protoLog.getTimeUnixNano()).isGreaterThan(0);
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
