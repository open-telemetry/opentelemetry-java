/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

/** Same as {@code FullConfigTest}, but using declarative configuration. */
@SuppressWarnings("InterruptedExceptionSwallowed")
public class FullDeclarativeConfigTest {

  private static final BlockingQueue<ExportTraceServiceRequest> otlpTraceRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<ExportMetricsServiceRequest> otlpMetricsRequests =
      new LinkedBlockingDeque<>();
  private static final BlockingQueue<ExportLogsServiceRequest> otlpLogsRequests =
      new LinkedBlockingDeque<>();

  @RegisterExtension
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
                  // OTLP logs
                  .addService(
                      new LogsServiceGrpc.LogsServiceImplBase() {
                        @Override
                        public void export(
                            ExportLogsServiceRequest request,
                            StreamObserver<ExportLogsServiceResponse> responseObserver) {
                          if (request.getResourceLogsCount() > 0) {
                            otlpLogsRequests.add(request);
                          }
                          responseObserver.onNext(ExportLogsServiceResponse.getDefaultInstance());
                          responseObserver.onCompleted();
                        }
                      })
                  .useBlockingTaskExecutor(true)
                  .build());
          sb.decorator(LoggingService.newDecorator());
        }
      };

  private OpenTelemetrySdk openTelemetrySdk;

  @BeforeEach
  void setUp(@TempDir Path tempDir) throws IOException {
    otlpTraceRequests.clear();
    otlpMetricsRequests.clear();
    otlpLogsRequests.clear();

    String endpoint = "http://localhost:" + server.httpPort();
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "resource:\n"
            + "  attributes:\n"
            + "    - name: service.name\n"
            + "      value: test\n"
            + "propagator:\n"
            + "  composite:\n"
            + "    - tracecontext:\n"
            + "    - baggage:\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        schedule_delay: 60000\n" // High delay for reliable export batches after
            // flushing
            + "        exporter:\n"
            + "          otlp_grpc:\n"
            + "            endpoint: "
            + endpoint
            + "\n"
            + "meter_provider:\n"
            + "  readers:\n"
            + "    - periodic:\n"
            + "        exporter:\n"
            + "          otlp_grpc:\n"
            + "            endpoint: "
            + endpoint
            + "\n"
            + "logger_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        schedule_delay: 60000\n" // High delay for reliable export batches after
            // flushing
            + "        exporter:\n"
            + "          otlp_grpc:\n"
            + "            endpoint: "
            + endpoint
            + "\n"
            + "instrumentation/development:\n"
            + "  java:\n"
            + "    otel_sdk:\n"
            + "      internal_telemetry_version: latest\n";

    Path path = tempDir.resolve("otel-config.yaml");
    Files.write(path, yaml.getBytes(StandardCharsets.UTF_8));

    // Initialize here so we can shutdown when done
    GlobalOpenTelemetry.resetForTest();
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", path.toString()));
    openTelemetrySdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setConfig(config)
            .setResultAsGlobal()
            .build()
            .getOpenTelemetrySdk();
  }

  @AfterEach
  void afterEach() {
    openTelemetrySdk.close();
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void configures() throws Exception {
    Collection<String> fields =
        GlobalOpenTelemetry.get().getPropagators().getTextMapPropagator().fields();
    List<String> keys = new ArrayList<>();
    keys.addAll(W3CTraceContextPropagator.getInstance().fields());
    keys.addAll(W3CBaggagePropagator.getInstance().fields());
    assertThat(fields).containsExactlyInAnyOrderElementsOf(keys);

    GlobalOpenTelemetry.get()
        .getTracer("test")
        .spanBuilder("test")
        .startSpan()
        .setAttribute("cat", "meow")
        .end();

    Meter meter = GlobalOpenTelemetry.get().getMeter("test");
    meter.counterBuilder("my-metric").build().add(1);

    Logger logger = GlobalOpenTelemetry.get().getLogsBridge().get("test");
    logger.logRecordBuilder().setBody("info log message").setSeverity(Severity.INFO).emit();

    openTelemetrySdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    openTelemetrySdk.getSdkLoggerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    openTelemetrySdk.getSdkMeterProvider().forceFlush().join(10, TimeUnit.SECONDS);

    await().untilAsserted(() -> assertThat(otlpTraceRequests).hasSize(1));

    ExportTraceServiceRequest traceRequest = otlpTraceRequests.take();
    List<KeyValue> spanResourceAttributes =
        traceRequest.getResourceSpans(0).getResource().getAttributesList();
    assertHasKeyValue(spanResourceAttributes, "service.name", "test");
    io.opentelemetry.proto.trace.v1.Span span =
        traceRequest.getResourceSpans(0).getScopeSpans(0).getSpans(0);
    assertHasKeyValue(span.getAttributesList(), "cat", "meow");

    // Flush again to get metric exporter metrics.
    openTelemetrySdk.getSdkMeterProvider().forceFlush().join(10, TimeUnit.SECONDS);
    // await on assertions since metrics may come in different order for BatchSpanProcessor,
    // exporter, or the ones we created in the test.
    await()
        .untilAsserted(
            () -> {
              ExportMetricsServiceRequest metricRequest = otlpMetricsRequests.take();

              assertThat(metricRequest.getResourceMetricsList())
                  .satisfiesExactly(
                      resourceMetrics -> {
                        List<KeyValue> metricResourceAttributes =
                            resourceMetrics.getResource().getAttributesList();
                        assertHasKeyValue(metricResourceAttributes, "service.name", "test");
                        assertThat(resourceMetrics.getScopeMetricsList())
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName()).isEqualTo("test");
                                  assertMetricNames(scopeMetrics, "my-metric");
                                })
                            // This verifies that MeterProvider was injected into OTLP exporters and
                            // the internal telemetry version was set.
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo(
                                          "io.opentelemetry.exporters.otlp_grpc_metric_exporter");
                                  assertMetricNames(
                                      scopeMetrics,
                                      "otel.sdk.exporter.metric_data_point.inflight",
                                      "otel.sdk.exporter.operation.duration",
                                      "otel.sdk.exporter.metric_data_point.exported");
                                })
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo(
                                          "io.opentelemetry.exporters.otlp_grpc_log_exporter");
                                  assertMetricNames(
                                      scopeMetrics,
                                      "otel.sdk.exporter.log.inflight",
                                      "otel.sdk.exporter.operation.duration",
                                      "otel.sdk.exporter.log.exported");
                                })
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo(
                                          "io.opentelemetry.exporters.otlp_grpc_span_exporter");
                                  assertMetricNames(
                                      scopeMetrics,
                                      "otel.sdk.exporter.span.inflight",
                                      "otel.sdk.exporter.operation.duration",
                                      "otel.sdk.exporter.span.exported");
                                })
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo("io.opentelemetry.sdk.logs");
                                  assertMetricNames(
                                      scopeMetrics,
                                      "otel.sdk.log.created",
                                      "otel.sdk.processor.log.processed",
                                      "otel.sdk.processor.log.queue.capacity",
                                      "otel.sdk.processor.log.queue.size");
                                })
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo("io.opentelemetry.sdk.trace");
                                  assertMetricNames(
                                      scopeMetrics,
                                      "otel.sdk.span.live",
                                      "otel.sdk.span.started",
                                      "otel.sdk.processor.span.processed",
                                      "otel.sdk.processor.span.queue.capacity",
                                      "otel.sdk.processor.span.queue.size");
                                });
                      });
            });

    await().untilAsserted(() -> assertThat(otlpLogsRequests).hasSize(1));
    ExportLogsServiceRequest logRequest = otlpLogsRequests.take();
    List<KeyValue> logResourceAttributes =
        logRequest.getResourceLogs(0).getResource().getAttributesList();
    assertHasKeyValue(logResourceAttributes, "service.name", "test");

    assertThat(logRequest.getResourceLogs(0).getScopeLogs(0).getLogRecordsList())
        .satisfiesExactlyInAnyOrder(
            logRecord -> {
              assertThat(logRecord.getBody().getStringValue()).isEqualTo("info log message");
              assertThat(logRecord.getSeverityNumberValue())
                  .isEqualTo(Severity.INFO.getSeverityNumber());
            });
  }

  private static void assertHasKeyValue(List<KeyValue> keyValues, String key, String value) {
    assertThat(keyValues)
        .contains(
            KeyValue.newBuilder()
                .setKey(key)
                .setValue(AnyValue.newBuilder().setStringValue(value))
                .build());
  }

  private static void assertMetricNames(ScopeMetrics scopeMetrics, String... names) {
    assertThat(
            scopeMetrics.getMetricsList().stream().map(Metric::getName).collect(Collectors.toSet()))
        .containsExactlyInAnyOrder(names);
  }
}
