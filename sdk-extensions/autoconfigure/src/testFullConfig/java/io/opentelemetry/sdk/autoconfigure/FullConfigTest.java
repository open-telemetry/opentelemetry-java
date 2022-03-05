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
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.extension.aws.AwsXrayPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
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
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.logs.LogEmitter;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.data.Severity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("InterruptedExceptionSwallowed")
class FullConfigTest {

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

  private SdkLogEmitterProvider logEmitterProvider;

  @BeforeEach
  void setUp() {
    otlpTraceRequests.clear();
    otlpMetricsRequests.clear();
    otlpLogsRequests.clear();

    String endpoint = "http://localhost:" + server.httpPort();
    System.setProperty("otel.exporter.otlp.endpoint", endpoint);
    System.setProperty("otel.exporter.otlp.timeout", "10000");

    // SdkLogEmitterProvider isn't globally accessible so we initialize here to get a reference
    logEmitterProvider =
        AutoConfiguredOpenTelemetrySdk.initialize()
            .getOpenTelemetrySdk()
            .getSdkLogEmitterProvider();
  }

  @Test
  void configures() throws Exception {
    Collection<String> fields =
        GlobalOpenTelemetry.get().getPropagators().getTextMapPropagator().fields();
    List<String> keys = new ArrayList<>();
    keys.addAll(W3CTraceContextPropagator.getInstance().fields());
    keys.addAll(W3CBaggagePropagator.getInstance().fields());
    keys.addAll(B3Propagator.injectingSingleHeader().fields());
    keys.addAll(B3Propagator.injectingMultiHeaders().fields());
    keys.addAll(JaegerPropagator.getInstance().fields());
    keys.addAll(OtTracePropagator.getInstance().fields());
    keys.addAll(AwsXrayPropagator.getInstance().fields());
    // Added by TestPropagatorProvider
    keys.add("test");
    assertThat(fields).containsExactlyInAnyOrderElementsOf(keys);

    GlobalOpenTelemetry.get().getMeterProvider().get("test").counterBuilder("test").build().add(1);

    GlobalOpenTelemetry.get()
        .getTracer("test")
        .spanBuilder("test")
        .startSpan()
        .setAttribute("cat", "meow")
        .setAttribute("dog", "bark")
        .end();

    Meter meter = GlobalOpenTelemetry.get().getMeter("test");
    meter
        .counterBuilder("my-metric")
        .build()
        .add(1, Attributes.builder().put("allowed", "bear").put("not allowed", "dog").build());
    meter.counterBuilder("my-other-metric").build().add(1);

    LogEmitter logEmitter = logEmitterProvider.get("test");
    logEmitter.logBuilder().setBody("debug log message").setSeverity(Severity.DEBUG).emit();
    logEmitter.logBuilder().setBody("info log message").setSeverity(Severity.INFO).emit();

    await().untilAsserted(() -> assertThat(otlpTraceRequests).hasSize(1));

    ExportTraceServiceRequest traceRequest = otlpTraceRequests.take();
    assertThat(traceRequest.getResourceSpans(0).getResource().getAttributesList())
        .contains(
            KeyValue.newBuilder()
                .setKey("service.name")
                .setValue(AnyValue.newBuilder().setStringValue("test").build())
                .build(),
            KeyValue.newBuilder()
                .setKey("cat")
                .setValue(AnyValue.newBuilder().setStringValue("meow").build())
                .build());
    io.opentelemetry.proto.trace.v1.Span span =
        traceRequest.getResourceSpans(0).getInstrumentationLibrarySpans(0).getSpans(0);
    // Dog dropped by attribute limit.
    assertThat(span.getAttributesList())
        .containsExactlyInAnyOrder(
            KeyValue.newBuilder()
                .setKey("configured")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("wrapped")
                .setValue(AnyValue.newBuilder().setIntValue(1).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("cat")
                .setValue(AnyValue.newBuilder().setStringValue("meow").build())
                .build());

    // await on assertions since metrics may come in different order for BatchSpanProcessor,
    // exporter, or the ones we
    // created in the test.
    await()
        .untilAsserted(
            () -> {
              ExportMetricsServiceRequest metricRequest = otlpMetricsRequests.take();
              assertThat(metricRequest.getResourceMetrics(0).getResource().getAttributesList())
                  .contains(
                      KeyValue.newBuilder()
                          .setKey("service.name")
                          .setValue(AnyValue.newBuilder().setStringValue("test").build())
                          .build(),
                      KeyValue.newBuilder()
                          .setKey("cat")
                          .setValue(AnyValue.newBuilder().setStringValue("meow").build())
                          .build());

              for (ResourceMetrics resourceMetrics : metricRequest.getResourceMetricsList()) {
                assertThat(resourceMetrics.getInstrumentationLibraryMetricsList())
                    .anySatisfy(
                        ilm ->
                            assertThat(ilm.getInstrumentationLibrary().getName())
                                .isEqualTo("test"));
                for (InstrumentationLibraryMetrics instrumentationLibraryMetrics :
                    resourceMetrics.getInstrumentationLibraryMetricsList()) {
                  for (Metric metric : instrumentationLibraryMetrics.getMetricsList()) {
                    // SPI was loaded
                    // MetricExporterCustomizer filters metrics not named my-metric
                    assertThat(metric.getName()).isEqualTo("my-metric");
                    // TestMeterProviderConfigurer configures a view that only passes on attribute
                    // named allowed
                    // configured-test
                    assertThat(getFirstDataPointLabels(metric))
                        .contains(
                            KeyValue.newBuilder()
                                .setKey("allowed")
                                .setValue(AnyValue.newBuilder().setStringValue("bear").build())
                                .build());
                  }
                }
              }
            });

    await().untilAsserted(() -> assertThat(otlpLogsRequests).hasSize(1));
    ExportLogsServiceRequest logRequest = otlpLogsRequests.take();
    assertThat(logRequest.getResourceLogs(0).getResource().getAttributesList())
        .contains(
            KeyValue.newBuilder()
                .setKey("service.name")
                .setValue(AnyValue.newBuilder().setStringValue("test").build())
                .build(),
            KeyValue.newBuilder()
                .setKey("cat")
                .setValue(AnyValue.newBuilder().setStringValue("meow").build())
                .build());
    // MetricExporterCustomizer filters logs not whose level is less than Severity.INFO
    LogRecord log = logRequest.getResourceLogs(0).getInstrumentationLibraryLogs(0).getLogRecords(0);
    assertThat(log.getBody().getStringValue()).isEqualTo("info log message");
    assertThat(log.getSeverityNumberValue()).isEqualTo(Severity.INFO.getSeverityNumber());
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
