/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import com.google.protobuf.Message;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.internal.common.util.SelfSignedCertificate;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayDeque;
import java.util.Queue;
import okio.Buffer;
import okio.GzipSource;
import okio.Okio;

class OtlpHttpServerExtension extends ServerExtension {

  final SelfSignedCertificate selfSignedCertificate;

  final Queue<ExportTraceServiceRequest> traceRequests = new ArrayDeque<>();
  final Queue<ExportMetricsServiceRequest> metricRequests = new ArrayDeque<>();
  final Queue<HttpResponse> responses = new ArrayDeque<>();
  final Queue<RequestHeaders> requestHeaders = new ArrayDeque<>();

  OtlpHttpServerExtension() {
    try {
      selfSignedCertificate = new SelfSignedCertificate();
    } catch (CertificateException e) {
      throw new IllegalStateException("Unable to setup certificate.", e);
    }
  }

  @Override
  protected void configure(ServerBuilder sb) {
    sb.service(
            "/v1/traces",
            httpService(traceRequests, ExportTraceServiceRequest.getDefaultInstance()))
        .service(
            "/v1/metrics",
            httpService(metricRequests, ExportMetricsServiceRequest.getDefaultInstance()));
    sb.tls(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey());
  }

  @SuppressWarnings("unchecked")
  private <T extends Message> HttpService httpService(Queue<T> queue, T defaultMessage) {
    return (ctx, req) ->
        HttpResponse.from(
            req.aggregate()
                .thenApply(
                    aggReq -> {
                      requestHeaders.add(aggReq.headers());
                      try {
                        byte[] requestBody =
                            maybeGzipInflate(aggReq.headers(), aggReq.content().array());
                        queue.add((T) defaultMessage.getParserForType().parseFrom(requestBody));
                      } catch (IOException e) {
                        return HttpResponse.of(HttpStatus.BAD_REQUEST);
                      }
                      HttpResponse response =
                          responses.peek() != null
                              ? responses.poll()
                              : HttpResponse.of(HttpStatus.OK);
                      return response;
                    }));
  }

  private static byte[] maybeGzipInflate(RequestHeaders requestHeaders, byte[] content)
      throws IOException {
    if (!requestHeaders.contains("content-encoding", "gzip")) {
      return content;
    }
    Buffer buffer = new Buffer();
    GzipSource gzipSource = new GzipSource(Okio.source(new ByteArrayInputStream(content)));
    gzipSource.read(buffer, Integer.MAX_VALUE);
    return buffer.readByteArray();
  }

  void reset() {
    traceRequests.clear();
    metricRequests.clear();
    requestHeaders.clear();
    responses.clear();
  }
}
