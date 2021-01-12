/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.CodedOutputStream;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.extension.otproto.SpanAdapter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class TraceMarshalerTest {
  private static final Resource RESOURCE =
      Resource.create(
          Attributes.builder()
              .put(AttributeKey.booleanKey("key_bool"), true)
              .put(AttributeKey.stringKey("key_string"), "string")
              .put(AttributeKey.longKey("key_int"), 100L)
              .put(AttributeKey.doubleKey("key_double"), 100.3)
              .put(
                  AttributeKey.stringArrayKey("key_string_array"),
                  Arrays.asList("string", "string"))
              .put(AttributeKey.longArrayKey("key_long_array"), Arrays.asList(12L, 23L))
              .put(AttributeKey.doubleArrayKey("key_double_array"), Arrays.asList(12.3, 23.1))
              .put(AttributeKey.booleanArrayKey("key_boolean_array"), Arrays.asList(true, false))
              .put(AttributeKey.booleanKey(""), true)
              .put(AttributeKey.stringKey("null_value"), null)
              .put(AttributeKey.stringKey("empty_value"), "")
              .build());

  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("name", null);
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.bytesToHex(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.bytesToHex(SPAN_ID_BYTES);

  private static final TraceState TRACE_STATE = TraceState.builder().build();
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TRACE_STATE);

  @Test
  void marshalAndSizeRequest() throws IOException {
    assertMarshalAndSize(Arrays.asList(testSpanData(), testSpanData(), testSpanData()));
  }

  @Test
  void marshalAndSizeRequest_Empty() throws IOException {
    assertMarshalAndSize(
        Collections.singletonList(
            TestSpanData.builder()
                .setTraceId("0123456789abcdef0123456789abcdef")
                .setSpanId("0123456789abcdef")
                .setKind(Span.Kind.INTERNAL)
                .setName("")
                .setStartEpochNanos(0)
                .setEndEpochNanos(0)
                .setHasEnded(true)
                .setStatus(StatusData.unset())
                .build()));
  }

  @Test
  void marshalAndSizeRequest_ErrorStatus() throws IOException {
    assertMarshalAndSize(
        Collections.singletonList(
            TestSpanData.builder()
                .setTraceId("0123456789abcdef0123456789abcdef")
                .setSpanId("0123456789abcdef")
                .setKind(Span.Kind.INTERNAL)
                .setName("")
                .setStartEpochNanos(0)
                .setEndEpochNanos(0)
                .setHasEnded(true)
                .setStatus(StatusData.error())
                .build()));
  }

  @Test
  void marshalAndSizeRequest_ValidParent() throws IOException {
    assertMarshalAndSize(
        Collections.singletonList(
            TestSpanData.builder()
                .setTraceId("0123456789abcdef0123456789abcdef")
                .setSpanId("0123456789abcdef")
                .setParentSpanContext(SPAN_CONTEXT)
                .setKind(Span.Kind.INTERNAL)
                .setName("")
                .setStartEpochNanos(0)
                .setEndEpochNanos(0)
                .setHasEnded(true)
                .setStatus(StatusData.unset())
                .build()));
  }

  @Test
  void marshalAndSizeRequest_InstrumentationLibrary() throws IOException {
    assertMarshalAndSize(
        Arrays.asList(
            testSpanDataWithInstrumentationLibrary(InstrumentationLibraryInfo.create("name", null)),
            testSpanDataWithInstrumentationLibrary(InstrumentationLibraryInfo.create("name", "")),
            testSpanDataWithInstrumentationLibrary(
                InstrumentationLibraryInfo.create("name", "version")),
            testSpanDataWithInstrumentationLibrary(InstrumentationLibraryInfo.getEmpty()),
            testSpanDataWithInstrumentationLibrary(InstrumentationLibraryInfo.create("", ""))));
  }

  private static SpanData testSpanDataWithInstrumentationLibrary(
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return TestSpanData.builder()
        .setInstrumentationLibraryInfo(instrumentationLibraryInfo)
        .setTraceId("0123456789abcdef0123456789abcdef")
        .setSpanId("0123456789abcdef")
        .setKind(Span.Kind.INTERNAL)
        .setName("")
        .setStartEpochNanos(0)
        .setEndEpochNanos(0)
        .setHasEnded(true)
        .setStatus(StatusData.unset())
        .build();
  }

  private static void assertMarshalAndSize(List<SpanData> spanDataList) throws IOException {
    ExportTraceServiceRequest protoRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(spanDataList))
            .build();
    TraceMarshaler.RequestMarshaler requestMarshaler =
        TraceMarshaler.RequestMarshaler.create(spanDataList);
    int protoSize = protoRequest.getSerializedSize();
    assertThat(requestMarshaler.getSerializedSize()).isEqualTo(protoSize);

    ExportTraceServiceRequest protoCustomRequest =
        TraceMarshaler.RequestMarshaler.create(spanDataList).toRequest();
    assertThat(protoCustomRequest.getSerializedSize()).isEqualTo(protoRequest.getSerializedSize());

    byte[] protoOutput = new byte[protoRequest.getSerializedSize()];
    protoRequest.writeTo(CodedOutputStream.newInstance(protoOutput));

    byte[] customOutput = new byte[requestMarshaler.getSerializedSize()];
    requestMarshaler.writeTo(CodedOutputStream.newInstance(customOutput));
    assertThat(customOutput).isEqualTo(protoOutput);

    byte[] protoCustomOutput = new byte[protoRequest.getSerializedSize()];
    protoCustomRequest.writeTo(CodedOutputStream.newInstance(protoCustomOutput));
    assertThat(protoCustomOutput).isEqualTo(protoOutput);
  }

  private static SpanData testSpanData() {
    return TestSpanData.builder()
        .setResource(RESOURCE)
        .setInstrumentationLibraryInfo(INSTRUMENTATION_LIBRARY_INFO)
        .setHasEnded(true)
        .setTraceId(TRACE_ID)
        .setSpanId(SPAN_ID)
        .setParentSpanContext(SpanContext.getInvalid())
        .setName("GET /api/endpoint")
        .setKind(Span.Kind.SERVER)
        .setStartEpochNanos(12345)
        .setEndEpochNanos(12349)
        .setAttributes(
            Attributes.builder()
                .put(AttributeKey.booleanKey("key_bool"), true)
                .put(AttributeKey.stringKey("key_string"), "string")
                .put(AttributeKey.longKey("key_int"), 100L)
                .put(AttributeKey.doubleKey("key_double"), 100.3)
                .build())
        .setTotalAttributeCount(2)
        .setEvents(
            Arrays.asList(
                EventData.create(12347, "my_event_1", Attributes.empty()),
                EventData.create(
                    12348,
                    "my_event_2",
                    Attributes.of(AttributeKey.longKey("event_attr_key"), 1234L))))
        .setTotalRecordedEvents(3)
        .setLinks(
            Arrays.asList(
                LinkData.create(SPAN_CONTEXT),
                LinkData.create(
                    SPAN_CONTEXT, Attributes.of(AttributeKey.stringKey("link_attr_key"), "value"))))
        .setTotalRecordedLinks(3)
        .setStatus(StatusData.ok())
        .build();
  }
}
