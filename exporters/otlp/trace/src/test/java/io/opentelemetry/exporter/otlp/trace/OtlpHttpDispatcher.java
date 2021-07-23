/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.rpc.Status;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.GzipSource;
import org.jetbrains.annotations.NotNull;

class OtlpHttpDispatcher extends Dispatcher {

  private final List<ExportTraceServiceRequest> requests;
  private final Queue<MockResponse> mockResponses;

  OtlpHttpDispatcher() {
    this.requests = new ArrayList<>();
    this.mockResponses = new ArrayDeque<>();
  }

  List<ExportTraceServiceRequest> getRequests() {
    return new ArrayList<>(requests);
  }

  void addMockResponse(MockResponse mockResponse) {
    this.mockResponses.add(mockResponse);
  }

  @NotNull
  @Override
  public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
    try {
      return dispatchInternal(recordedRequest);
    } catch (RuntimeException e) {
      return errorResponse(500, "An unexpected error occurred: " + e.getMessage());
    }
  }

  private MockResponse dispatchInternal(RecordedRequest recordedRequest) {
    if (!mockResponses.isEmpty()) {
      return mockResponses.poll();
    }

    if (!"POST".equals(recordedRequest.getMethod())) {
      return errorResponse(405, "Unsupported method.");
    }

    if (!"/v1/traces".equals(recordedRequest.getPath())) {
      return errorResponse(404, "Unsupported path.");
    }

    String contentType = recordedRequest.getHeader("Content-Type");
    if (contentType == null || !contentType.contains("application/x-protobuf")) {
      return errorResponse(404, "Unsupported Content-Type.");
    }

    byte[] body = recordedRequest.getBody().readByteArray();
    String contentEncoding = recordedRequest.getHeader("Content-Encoding");
    if ("gzip".equals(contentEncoding)) {
      body = gzipDecompress(body);
    } else if (contentEncoding != null) {
      return errorResponse(400, "Unsupported Content-Encoding.");
    }

    ExportTraceServiceRequest exportTraceServiceRequest = parseBody(body);
    requests.add(exportTraceServiceRequest);

    return successResponse();
  }

  static MockResponse errorResponse(int statusCode, String message) {
    Status status = Status.newBuilder().setMessage(message).build();
    return buildResponse(statusCode, status);
  }

  private static MockResponse successResponse() {
    ExportTraceServiceResponse exportTraceServiceResponse =
        ExportTraceServiceResponse.newBuilder().build();
    return buildResponse(200, exportTraceServiceResponse);
  }

  private static <T extends Message> MockResponse buildResponse(int statusCode, T message) {
    MockResponse response = new MockResponse().setResponseCode(statusCode);
    return response
        .setHeader("Content-Type", "application/x-protobuf")
        .setBody(new Buffer().write(message.toByteArray()));
  }

  private static byte[] gzipDecompress(byte[] bytes) {
    try {
      Buffer result = new Buffer();
      GzipSource source = new GzipSource(new Buffer().write(bytes));
      while (source.read(result, Integer.MAX_VALUE) != -1) {}
      return result.readByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to decompress payload.", e);
    }
  }

  private static ExportTraceServiceRequest parseBody(byte[] bytes) {
    try {
      return ExportTraceServiceRequest.parseFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException("Unable to parse Protobuf request body.", e);
    }
  }
}
