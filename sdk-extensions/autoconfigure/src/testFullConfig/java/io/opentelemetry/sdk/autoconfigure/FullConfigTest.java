/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
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
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("InterruptedExceptionSwallowed")
public class FullConfigTest {

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
                          try {
                            RequestHeaders headers =
                                ServiceRequestContext.current().request().headers();
                            assertThat(headers.get("cat")).isEqualTo("meow");
                            assertThat(headers.get("dog")).isEqualTo("bark");
                          } catch (Throwable t) {
                            responseObserver.onError(t);
                            return;
                          }
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
                          try {
                            RequestHeaders headers =
                                ServiceRequestContext.current().request().headers();
                            assertThat(headers.get("cat")).isEqualTo("meow");
                            assertThat(headers.get("dog")).isEqualTo("bark");
                          } catch (Throwable t) {
                            responseObserver.onError(t);
                            return;
                          }
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
                          try {
                            RequestHeaders headers =
                                ServiceRequestContext.current().request().headers();
                            assertThat(headers.get("cat")).isEqualTo("meow");
                            assertThat(headers.get("dog")).isEqualTo("bark");
                          } catch (Throwable t) {
                            responseObserver.onError(t);
                            return;
                          }
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
  void setUp() {
    otlpTraceRequests.clear();
    otlpMetricsRequests.clear();
    otlpLogsRequests.clear();

    String endpoint = "http://localhost:" + server.httpPort();
    System.setProperty("otel.exporter.otlp.endpoint", endpoint);
    System.setProperty("otel.exporter.otlp.timeout", "10000");
    // Set log exporter interval to a high value and rely on flushing to produce reliable export
    // batches
    System.setProperty("otel.blrp.schedule.delay", "60000");

    // Initialize here so we can shutdown when done
    GlobalOpenTelemetry.resetForTest();
    openTelemetrySdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
  }

  @AfterEach
  void afterEach() {
    openTelemetrySdk.close();
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  @SuppressWarnings("deprecation")
  void configures() throws Exception {
    Collection<String> fields =
        GlobalOpenTelemetry.get().getPropagators().getTextMapPropagator().fields();
    List<String> keys = new ArrayList<>();
    keys.addAll(W3CTraceContextPropagator.getInstance().fields());
    keys.addAll(W3CBaggagePropagator.getInstance().fields());
    keys.addAll(B3Propagator.injectingSingleHeader().fields());
    keys.addAll(B3Propagator.injectingMultiHeaders().fields());
    keys.addAll(
        io.opentelemetry.extension.trace.propagation.JaegerPropagator.getInstance().fields());
    keys.addAll(
        io.opentelemetry.extension.trace.propagation.OtTracePropagator.getInstance().fields());
    // Added by TestPropagatorProvider
    keys.add("test");
    assertThat(fields).containsExactlyInAnyOrderElementsOf(keys);

    GlobalOpenTelemetry.get().getMeterProvider().get("test").counterBuilder("test").build().add(1);

    GlobalOpenTelemetry.get()
        .getTracer("test")
        .spanBuilder("test")
        .startSpan()
        .setAttribute("cat", "meow")
        .end();

    Meter meter = GlobalOpenTelemetry.get().getMeter("test");
    meter
        .counterBuilder("my-metric")
        .build()
        .add(1, Attributes.builder().put("allowed", "bear").put("not allowed", "dog").build());
    meter.counterBuilder("my-other-metric").build().add(1);

    Logger logger = GlobalOpenTelemetry.get().getLogsBridge().get("test");
    logger.logRecordBuilder().setBody("debug log message").setSeverity(Severity.DEBUG).emit();
    logger.logRecordBuilder().setBody("info log message").setSeverity(Severity.INFO).emit();

    openTelemetrySdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    openTelemetrySdk.getSdkLoggerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    openTelemetrySdk.getSdkMeterProvider().forceFlush().join(10, TimeUnit.SECONDS);

    await().untilAsserted(() -> assertThat(otlpTraceRequests).hasSize(1));

    ExportTraceServiceRequest traceRequest = otlpTraceRequests.take();
    List<KeyValue> spanResourceAttributes =
        traceRequest.getResourceSpans(0).getResource().getAttributesList();
    assertHasKeyValue(spanResourceAttributes, "service.name", "test");
    assertHasKeyValue(spanResourceAttributes, "cat", "meow");
    io.opentelemetry.proto.trace.v1.Span span =
        traceRequest.getResourceSpans(0).getScopeSpans(0).getSpans(0);
    assertHasKeyValue(span.getAttributesList(), "configured", true);
    assertHasKeyValue(span.getAttributesList(), "wrapped", 1);
    assertHasKeyValue(span.getAttributesList(), "cat", "meow");
    assertHasKeyValue(span.getAttributesList(), "extra-key", "extra-value");

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
                        assertHasKeyValue(metricResourceAttributes, "cat", "meow");
                        assertThat(resourceMetrics.getScopeMetricsList())
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName()).isEqualTo("test");
                                  assertThat(scopeMetrics.getMetricsList())
                                      .satisfiesExactly(
                                          metric -> {
                                            // SPI was loaded
                                            assertThat(metric.getName()).isEqualTo("my-metric");
                                            // TestMeterProviderConfigurer configures a view that
                                            // only passes an attribute named "allowed"
                                            // configured-test
                                            assertHasKeyValue(
                                                getFirstDataPointLabels(metric), "allowed", "bear");
                                          });
                                })
                            // This verifies that AutoConfigureListener was invoked and the OTLP
                            // span / log exporters received the autoconfigured OpenTelemetrySdk
                            // instance as well as setting telemetry version.
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo(
                                          "io.opentelemetry.exporters.otlp_grpc_metric_exporter");
                                  assertThat(scopeMetrics.getMetricsList())
                                      .satisfiesExactlyInAnyOrder(
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo(
                                                      "otel.sdk.exporter.metric_data_point.inflight"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo(
                                                      "otel.sdk.exporter.operation.duration"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo(
                                                      "otel.sdk.exporter.metric_data_point.exported"));
                                })
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo(
                                          "io.opentelemetry.exporters.otlp_grpc_log_exporter");
                                  assertThat(scopeMetrics.getMetricsList())
                                      .satisfiesExactlyInAnyOrder(
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.exporter.log.inflight"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo(
                                                      "otel.sdk.exporter.operation.duration"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.exporter.log.exported"));
                                })
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo(
                                          "io.opentelemetry.exporters.otlp_grpc_span_exporter");
                                  assertThat(scopeMetrics.getMetricsList())
                                      .satisfiesExactlyInAnyOrder(
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.exporter.span.inflight"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo(
                                                      "otel.sdk.exporter.operation.duration"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.exporter.span.exported"));
                                })
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo("io.opentelemetry.sdk.logs");
                                  assertThat(scopeMetrics.getMetricsList())
                                      .satisfiesExactlyInAnyOrder(
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.log.created"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.processor.log.processed"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo(
                                                      "otel.sdk.processor.log.queue.capacity"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.processor.log.queue.size"));
                                })
                            .anySatisfy(
                                scopeMetrics -> {
                                  assertThat(scopeMetrics.getScope().getName())
                                      .isEqualTo("io.opentelemetry.sdk.trace");
                                  assertThat(scopeMetrics.getMetricsList())
                                      .satisfiesExactlyInAnyOrder(
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.span.live"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.span.started"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.processor.span.processed"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo(
                                                      "otel.sdk.processor.span.queue.capacity"),
                                          metric ->
                                              assertThat(metric.getName())
                                                  .isEqualTo("otel.sdk.processor.span.queue.size"));
                                });
                      });
            });

    await().untilAsserted(() -> assertThat(otlpLogsRequests).hasSize(1));
    ExportLogsServiceRequest logRequest = otlpLogsRequests.take();
    List<KeyValue> logResourceAttributes =
        logRequest.getResourceLogs(0).getResource().getAttributesList();
    assertHasKeyValue(logResourceAttributes, "service.name", "test");
    assertHasKeyValue(logResourceAttributes, "cat", "meow");

    assertThat(logRequest.getResourceLogs(0).getScopeLogs(0).getLogRecordsList())
        // LogRecordCustomizer customizes BatchLogProcessor to add an extra attribute on every log
        // record
        .allSatisfy(
            logRecord ->
                assertHasKeyValue(logRecord.getAttributesList(), "extra-key", "extra-value"))
        .satisfiesExactlyInAnyOrder(
            logRecord -> {
              // LogRecordCustomizer filters logs not whose level is less than Severity.INFO
              assertThat(logRecord.getBody().getStringValue()).isEqualTo("info log message");
              assertThat(logRecord.getSeverityNumberValue())
                  .isEqualTo(Severity.INFO.getSeverityNumber());
            });
  }

  private static void assertHasKeyValue(List<KeyValue> keyValues, String key, boolean value) {
    assertThat(keyValues)
        .contains(
            KeyValue.newBuilder()
                .setKey(key)
                .setValue(AnyValue.newBuilder().setBoolValue(value))
                .build());
  }

  private static void assertHasKeyValue(List<KeyValue> keyValues, String key, long value) {
    assertThat(keyValues)
        .contains(
            KeyValue.newBuilder()
                .setKey(key)
                .setValue(AnyValue.newBuilder().setIntValue(value))
                .build());
  }

  private static void assertHasKeyValue(List<KeyValue> keyValues, String key, String value) {
    assertThat(keyValues)
        .contains(
            KeyValue.newBuilder()
                .setKey(key)
                .setValue(AnyValue.newBuilder().setStringValue(value))
                .build());
  }

  private static List<KeyValue> getFirstDataPointLabels(Metric metric) {
    switch (metric.getDataCase()) {
      case GAUGE:
        return metric.getGauge().getDataPoints(0).getAttributesList();
      case SUM:
        return metric.getSum().getDataPoints(0).getAttributesList();
      case HISTOGRAM:
        return metric.getHistogram().getDataPoints(0).getAttributesList();
      case SUMMARY:
        return metric.getSummary().getDataPoints(0).getAttributesList();
      default:
        throw new IllegalArgumentException(
            "Unrecognized metric data case: " + metric.getDataCase().name());
    }
  }
}
