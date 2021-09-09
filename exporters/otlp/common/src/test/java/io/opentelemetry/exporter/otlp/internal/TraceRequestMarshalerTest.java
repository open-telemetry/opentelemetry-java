/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class TraceRequestMarshalerTest {

  private static final EasyRandom EASY_RANDOM =
      new EasyRandom(
          new EasyRandomParameters()
              // EasyRandom uses a fixed seed by default for reproducibility but we'd rather have
              // increased coverage over CI runs.
              .seed(new Random().nextLong())
              .stringLengthRange(0, 20)
              .collectionSizeRange(0, 5)
              .randomizerRegistry(SpanDataRandomizerRegistry.INSTANCE));

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
  private static final String TRACE_ID = "00000000000000000000000001020304";
  private static final String SPAN_ID = "0000000004030201";

  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(
          "0123456789abcdef0123456789abcdef",
          "0123456789abcdef",
          TraceFlags.getSampled(),
          TraceState.getDefault());
  private static final SpanContext PARENT_SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());

  @Test
  void marshalAndSizeRequest() throws IOException {
    assertMarshalAndSize(Arrays.asList(testSpanData(), testSpanData(), testSpanData()));
  }

  @Test
  void marshalAndSizeRequest_Empty() throws IOException {
    assertMarshalAndSize(
        Collections.singletonList(
            TestSpanData.builder()
                .setSpanContext(SPAN_CONTEXT)
                .setKind(SpanKind.INTERNAL)
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
                .setSpanContext(SPAN_CONTEXT)
                .setKind(SpanKind.INTERNAL)
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
                .setSpanContext(SPAN_CONTEXT)
                .setParentSpanContext(PARENT_SPAN_CONTEXT)
                .setKind(SpanKind.INTERNAL)
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
            testSpanDataWithInstrumentationLibrary(InstrumentationLibraryInfo.empty()),
            testSpanDataWithInstrumentationLibrary(InstrumentationLibraryInfo.create("", ""))));
  }

  @RepeatedTest(100)
  void randomData() throws Exception {
    SpanData span = EASY_RANDOM.nextObject(SpanData.class);

    ExportTraceServiceRequest protoRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(Collections.singletonList(span)))
            .build();
    TraceRequestMarshaler requestMarshaler =
        TraceRequestMarshaler.create(Collections.singletonList(span));

    byte[] protoOutput = protoRequest.toByteArray();

    ByteArrayOutputStream customOutput =
        new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
    requestMarshaler.writeBinaryTo(customOutput);
    byte[] customOutputBytes = customOutput.toByteArray();
    if (!Arrays.equals(customOutputBytes, protoOutput)) {
      String reverse = "<invalid>";
      try {
        reverse = ExportTraceServiceRequest.parseFrom(customOutputBytes).toString();
      } catch (IOException e) {
        reverse += " " + e.getMessage();
      }
      throw new AssertionError(
          "Serialization through TraceMarshaller does not match serialization through "
              + "protobuf library.\nspan: "
              + span
              + "\nprotobuf: "
              + protoRequest
              + "\nparsed from marshaler output: "
              + reverse);
    }
  }

  private static SpanData testSpanDataWithInstrumentationLibrary(
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return TestSpanData.builder()
        .setInstrumentationLibraryInfo(instrumentationLibraryInfo)
        .setSpanContext(SPAN_CONTEXT)
        .setKind(SpanKind.INTERNAL)
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
    TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(spanDataList);
    int protoSize = protoRequest.getSerializedSize();
    assertThat(requestMarshaler.getBinarySerializedSize()).isEqualTo(protoSize);

    ByteArrayOutputStream protoOutput = new ByteArrayOutputStream(protoRequest.getSerializedSize());
    protoRequest.writeTo(protoOutput);

    ByteArrayOutputStream customOutput =
        new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
    requestMarshaler.writeBinaryTo(customOutput);
    assertThat(customOutput.toByteArray()).isEqualTo(protoOutput.toByteArray());
  }

  private static SpanData testSpanData() {
    return TestSpanData.builder()
        .setResource(RESOURCE)
        .setInstrumentationLibraryInfo(INSTRUMENTATION_LIBRARY_INFO)
        .setHasEnded(true)
        .setSpanContext(SPAN_CONTEXT)
        .setParentSpanContext(SpanContext.getInvalid())
        .setName("GET /api/endpoint")
        .setKind(SpanKind.SERVER)
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
                LinkData.create(PARENT_SPAN_CONTEXT),
                LinkData.create(
                    PARENT_SPAN_CONTEXT,
                    Attributes.of(AttributeKey.stringKey("link_attr_key"), "value"))))
        .setTotalRecordedLinks(3)
        .setStatus(StatusData.ok())
        .build();
  }
}
