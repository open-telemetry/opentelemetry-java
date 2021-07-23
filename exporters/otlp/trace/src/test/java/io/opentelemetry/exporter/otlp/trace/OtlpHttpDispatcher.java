/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.UnsafeByteOperations;
import com.google.protobuf.util.JsonFormat;
import com.google.rpc.Status;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.exporter.otlp.trace.OtlpHttpSpanExporterBuilder.Encoding;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.GzipSource;
import org.curioswitch.common.protobuf.json.MessageMarshaller;
import org.jetbrains.annotations.NotNull;

class OtlpHttpDispatcher extends Dispatcher {

  private static final MessageMarshaller MARSHALLER =
      MessageMarshaller.builder()
          .register(ExportTraceServiceResponse.class)
          .register(Status.class)
          .omittingInsignificantWhitespace(true)
          .build();

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
      return errorResponse(Encoding.JSON, 500, "An unexpected error occurred: " + e.getMessage());
    }
  }

  private MockResponse dispatchInternal(RecordedRequest recordedRequest) {
    if (!mockResponses.isEmpty()) {
      return mockResponses.poll();
    }

    Encoding encoding;
    String contentType = recordedRequest.getHeader("Content-Type");
    if (contentType != null && contentType.contains("application/json")) {
      encoding = Encoding.JSON;
    } else if (contentType != null && contentType.contains("application/x-protobuf")) {
      encoding = Encoding.PROTOBUF;
    } else {
      return errorResponse(Encoding.JSON, 404, "Unsupported Content-Type.");
    }

    if (!"POST".equals(recordedRequest.getMethod())) {
      return errorResponse(encoding, 405, "Unsupported method.");
    }

    if (!"/v1/traces".equals(recordedRequest.getPath())) {
      return errorResponse(encoding, 404, "Unsupported path.");
    }

    byte[] body = recordedRequest.getBody().readByteArray();
    String contentEncoding = recordedRequest.getHeader("Content-Encoding");
    if ("gzip".equals(contentEncoding)) {
      body = gzipDecompress(body);
    } else if (contentEncoding != null) {
      return errorResponse(encoding, 400, "Unsupported Content-Encoding.");
    }

    ExportTraceServiceRequest exportTraceServiceRequest = parseBody(encoding, body);
    requests.add(exportTraceServiceRequest);

    return successResponse(encoding);
  }

  static MockResponse errorResponse(Encoding encoding, int statusCode, String message) {
    Status status = Status.newBuilder().setMessage(message).build();
    return buildResponse(encoding, statusCode, status);
  }

  private static MockResponse successResponse(Encoding encoding) {
    ExportTraceServiceResponse exportTraceServiceResponse =
        ExportTraceServiceResponse.newBuilder().build();
    return buildResponse(encoding, 200, exportTraceServiceResponse);
  }

  private static <T extends Message> MockResponse buildResponse(
      Encoding encoding, int statusCode, T message) {
    MockResponse response = new MockResponse().setResponseCode(statusCode);
    switch (encoding) {
      case JSON:
        response.setHeader("Content-Type", "application/json");
        try {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          MARSHALLER.writeValue(message, baos);
          return response.setBody(new Buffer().write(baos.toByteArray()));
        } catch (IOException e) {
          throw new IllegalStateException("Unable to build JSON response body.", e);
        }
      case PROTOBUF:
        return response
            .setHeader("Content-Type", "application/x-protobuf")
            .setBody(new Buffer().write(message.toByteArray()));
    }
    throw new IllegalStateException("Unsupported encoding " + encoding.name());
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

  private static ExportTraceServiceRequest parseBody(Encoding encoding, byte[] bytes) {
    switch (encoding) {
      case JSON:
        try {
          ExportTraceServiceRequest.Builder builder = ExportTraceServiceRequest.newBuilder();
          JsonFormat.parser()
              .merge(new InputStreamReader(new ByteArrayInputStream(bytes), UTF_8), builder);
          ExportTraceServiceRequest request = builder.build();
          return fixJsonHexDecoding(request);
        } catch (IOException e) {
          throw new IllegalStateException("Unable to parse JSON request body.", e);
        }
      case PROTOBUF:
        try {
          return ExportTraceServiceRequest.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
          throw new IllegalStateException("Unable to parse Protobuf request body.", e);
        }
    }
    throw new IllegalStateException("Unsupported encoding " + encoding.name());
  }

  /**
   * Return a copy of the request after fixing {@code trace_id} and {@code span_id} decoding issues.
   *
   * <p>The OTLP HTTP JSON specification states that {@code trace_id} and {@code span_id} are
   * represented as hex strings instead of base64 encoded. However, its not possible to specify
   * custom deserialization logic for {@link JsonFormat}, which automatically base64 decodes all
   * byte arrays. To get around this, the {@code trace_id} and {@code span_id} fields are base64
   * encoded (to revert the erroneous base64 decode) before being parsed as a base 16 hex string.
   */
  private static ExportTraceServiceRequest fixJsonHexDecoding(ExportTraceServiceRequest request) {
    ExportTraceServiceRequest.Builder requestBuilder = ExportTraceServiceRequest.newBuilder();
    for (ResourceSpans resourceSpans : request.getResourceSpansList()) {
      ResourceSpans.Builder resourceSpansBuilder =
          resourceSpans.toBuilder().clearInstrumentationLibrarySpans();
      for (InstrumentationLibrarySpans librarySpans :
          resourceSpans.getInstrumentationLibrarySpansList()) {
        InstrumentationLibrarySpans.Builder librarySpansBuilder =
            librarySpans.toBuilder().clearSpans();
        for (Span span : librarySpans.getSpansList()) {
          librarySpansBuilder.addSpans(
              span.toBuilder().setTraceId(hex(span.getTraceId())).setSpanId(hex(span.getSpanId())));
        }
        resourceSpansBuilder.addInstrumentationLibrarySpans(librarySpansBuilder);
      }
      requestBuilder.addResourceSpans(resourceSpansBuilder);
    }
    return requestBuilder.build();
  }

  private static ByteString hex(ByteString base64DecodedHex) {
    String hexString = BaseEncoding.base64().encode(base64DecodedHex.toByteArray());
    return UnsafeByteOperations.unsafeWrap(
        OtelEncodingUtils.bytesFromBase16(hexString, hexString.length()));
  }
}
