/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

class OtlpGrpcServerExtension extends ServerExtension {

  final Queue<ExportTraceServiceRequest> traceRequests = new ArrayDeque<>();
  final Queue<ExportMetricsServiceRequest> metricRequests = new ArrayDeque<>();
  final Queue<ExportLogsServiceRequest> logRequests = new ArrayDeque<>();
  final Queue<Status> responseStatuses = new ArrayDeque<>();
  final Queue<RequestHeaders> requestHeaders = new ArrayDeque<>();
  private final SelfSignedCertificateExtension certificate;

  OtlpGrpcServerExtension(SelfSignedCertificateExtension certificate) {
    this.certificate = certificate;
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
                    exportHelper(
                        traceRequests,
                        ExportTraceServiceResponse.getDefaultInstance(),
                        request,
                        responseObserver);
                  }
                })
            .addService(
                new MetricsServiceGrpc.MetricsServiceImplBase() {
                  @Override
                  public void export(
                      ExportMetricsServiceRequest request,
                      StreamObserver<ExportMetricsServiceResponse> responseObserver) {
                    exportHelper(
                        metricRequests,
                        ExportMetricsServiceResponse.getDefaultInstance(),
                        request,
                        responseObserver);
                  }
                })
            .addService(
                new LogsServiceGrpc.LogsServiceImplBase() {
                  @Override
                  public void export(
                      ExportLogsServiceRequest request,
                      StreamObserver<ExportLogsServiceResponse> responseObserver) {
                    exportHelper(
                        logRequests,
                        ExportLogsServiceResponse.getDefaultInstance(),
                        request,
                        responseObserver);
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

  private <RequestT, ResponseT> void exportHelper(
      Queue<RequestT> requests,
      ResponseT defaultResponse,
      RequestT request,
      StreamObserver<ResponseT> responseObserver) {
    requests.add(request);
    Status responseStatus = responseStatuses.peek() != null ? responseStatuses.poll() : Status.OK;
    if (responseStatus.isOk()) {
      responseObserver.onNext(defaultResponse);
      responseObserver.onCompleted();
      return;
    }
    responseObserver.onError(responseStatus.asRuntimeException());
  }

  void reset() {
    traceRequests.clear();
    metricRequests.clear();
    logRequests.clear();
    requestHeaders.clear();
    responseStatuses.clear();
  }

  static SpanData generateFakeSpan() {
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

  static MetricData generateFakeMetric() {
    return MetricData.createLongSum(
        Resource.empty(),
        InstrumentationScopeInfo.empty(),
        "metric_name",
        "metric_description",
        "ms",
        ImmutableSumData.create(
            false,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                ImmutableLongPointData.create(
                    MILLISECONDS.toNanos(System.currentTimeMillis()),
                    MILLISECONDS.toNanos(System.currentTimeMillis()),
                    Attributes.of(stringKey("key"), "value"),
                    10))));
  }

  static LogData generateFakeLog() {
    return LogDataBuilder.create(Resource.empty(), InstrumentationScopeInfo.empty())
        .setEpoch(Instant.now())
        .setBody("log body")
        .build();
  }
}
