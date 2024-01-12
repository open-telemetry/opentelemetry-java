/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static io.opentelemetry.exporter.zipkin.ZipkinTestUtil.PARENT_SPAN_ID;
import static io.opentelemetry.exporter.zipkin.ZipkinTestUtil.SPAN_ID;
import static io.opentelemetry.exporter.zipkin.ZipkinTestUtil.TRACE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.Encoding;
import zipkin2.reporter.SpanBytesEncoder;

class BytesEncoderAdapterTest {

  /** Contains {@link Span#localEndpoint()} to ensure would be encoded differently. */
  private final Span testSpan =
      Span.newBuilder()
          .traceId(TRACE_ID)
          .parentId(PARENT_SPAN_ID)
          .id(SPAN_ID)
          .localEndpoint(Endpoint.newBuilder().serviceName("test").build())
          .build();

  @Test
  void testJsonV2() {
    BytesEncoderAdapter adapter = new BytesEncoderAdapter(zipkin2.codec.SpanBytesEncoder.JSON_V2);
    assertThat(adapter.encoding()).isEqualTo(Encoding.JSON);
    assertThat(adapter.encode(testSpan)).isEqualTo(SpanBytesEncoder.JSON_V2.encode(testSpan));
    assertThat(adapter.sizeInBytes(testSpan))
        .isEqualTo(SpanBytesEncoder.JSON_V2.sizeInBytes(testSpan));
    assertThat(adapter).hasToString(SpanBytesEncoder.JSON_V2.toString());
  }

  @Test
  void testProtobuf() {
    BytesEncoderAdapter adapter = new BytesEncoderAdapter(zipkin2.codec.SpanBytesEncoder.PROTO3);
    assertThat(adapter.encoding()).isEqualTo(Encoding.PROTO3);
    assertThat(adapter.encode(testSpan)).isEqualTo(SpanBytesEncoder.PROTO3.encode(testSpan));
    assertThat(adapter.sizeInBytes(testSpan))
        .isEqualTo(SpanBytesEncoder.PROTO3.sizeInBytes(testSpan));
    assertThat(adapter).hasToString(SpanBytesEncoder.PROTO3.toString());
  }

  @Test
  @SuppressWarnings("deprecation") // we have to use the deprecated thrift encoding to test it
  void testThrift() {
    BytesEncoderAdapter adapter = new BytesEncoderAdapter(zipkin2.codec.SpanBytesEncoder.THRIFT);
    assertThat(adapter.encoding()).isEqualTo(Encoding.THRIFT);
    assertThat(adapter.encode(testSpan)).isEqualTo(SpanBytesEncoder.THRIFT.encode(testSpan));
    assertThat(adapter.sizeInBytes(testSpan))
        .isEqualTo(SpanBytesEncoder.THRIFT.sizeInBytes(testSpan));
    assertThat(adapter).hasToString(SpanBytesEncoder.THRIFT.toString());
  }

  @Test
  void testJsonV1() {
    BytesEncoderAdapter adapter = new BytesEncoderAdapter(zipkin2.codec.SpanBytesEncoder.JSON_V1);
    assertThat(adapter.encoding()).isEqualTo(Encoding.JSON);
    assertThat(adapter.encode(testSpan)).isEqualTo(SpanBytesEncoder.JSON_V1.encode(testSpan));
    assertThat(adapter.sizeInBytes(testSpan))
        .isEqualTo(SpanBytesEncoder.JSON_V1.sizeInBytes(testSpan));
    assertThat(adapter).hasToString(SpanBytesEncoder.JSON_V1.toString());
  }
}
