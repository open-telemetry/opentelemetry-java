/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import java.util.ArrayDeque;
import java.util.Queue;

class OtlpGrpcServerExtension extends ServerExtension {

  final Queue<ExportTraceServiceRequest> traceRequests = new ArrayDeque<>();
  final Queue<ExportMetricsServiceRequest> metricRequests = new ArrayDeque<>();
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
    requestHeaders.clear();
    responseStatuses.clear();
  }
}
