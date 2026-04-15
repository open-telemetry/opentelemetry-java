/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class LowAllocationTraceRequestMarshalerTest {

  private static final AttributeKey<Boolean> KEY_BOOL = AttributeKey.booleanKey("key_bool");
  private static final AttributeKey<String> KEY_STRING = AttributeKey.stringKey("key_string");
  private static final AttributeKey<Long> KEY_INT = AttributeKey.longKey("key_int");
  private static final AttributeKey<Double> KEY_DOUBLE = AttributeKey.doubleKey("key_double");
  private static final AttributeKey<List<String>> KEY_STRING_ARRAY =
      AttributeKey.stringArrayKey("key_string_array");
  private static final AttributeKey<List<Long>> KEY_LONG_ARRAY =
      AttributeKey.longArrayKey("key_long_array");
  private static final AttributeKey<List<Double>> KEY_DOUBLE_ARRAY =
      AttributeKey.doubleArrayKey("key_double_array");
  private static final AttributeKey<List<Boolean>> KEY_BOOLEAN_ARRAY =
      AttributeKey.booleanArrayKey("key_boolean_array");
  private static final AttributeKey<Value<?>> KEY_BYTES = AttributeKey.valueKey("key_bytes");
  private static final AttributeKey<Value<?>> KEY_MAP = AttributeKey.valueKey("key_map");
  private static final AttributeKey<Value<?>> KEY_HETEROGENEOUS_ARRAY =
      AttributeKey.valueKey("key_heterogeneous_array");
  private static final AttributeKey<Value<?>> KEY_EMPTY = AttributeKey.valueKey("key_empty");
  private static final AttributeKey<String> LINK_ATTR_KEY = AttributeKey.stringKey("link_attr_key");

  private static final Resource RESOURCE =
      Resource.create(
          Attributes.builder()
              .put(KEY_BOOL, true)
              .put(KEY_STRING, "string")
              .put(KEY_INT, 100L)
              .put(KEY_DOUBLE, 100.3)
              .put(KEY_STRING_ARRAY, Arrays.asList("string", "string"))
              .put(KEY_LONG_ARRAY, Arrays.asList(12L, 23L))
              .put(KEY_DOUBLE_ARRAY, Arrays.asList(12.3, 23.1))
              .put(KEY_BOOLEAN_ARRAY, Arrays.asList(true, false))
              .put(KEY_BYTES, Value.of(new byte[] {1, 2, 3}))
              .put(KEY_MAP, Value.of(KeyValue.of("nested", Value.of("value"))))
              .put(KEY_HETEROGENEOUS_ARRAY, Value.of(Value.of("string"), Value.of(123L)))
              .put(KEY_EMPTY, Value.empty())
              .build());

  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("name");
  private static final String TRACE_ID = "7b2e170db4df2d593ddb4ddf2ddf2d59";
  private static final String SPAN_ID = "170d3ddb4d23e81f";
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());

  private final List<SpanData> spanDataList = createSpanDataList();

  private static List<SpanData> createSpanDataList() {
    List<SpanData> spanDataList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      spanDataList.add(createSpanData());
    }
    return spanDataList;
  }

  private static SpanData createSpanData() {
    return TestSpanData.builder()
        .setResource(RESOURCE)
        .setInstrumentationScopeInfo(INSTRUMENTATION_SCOPE_INFO)
        .setHasEnded(true)
        .setSpanContext(SPAN_CONTEXT)
        .setParentSpanContext(SpanContext.getInvalid())
        .setName("GET /api/endpoint")
        .setKind(SpanKind.SERVER)
        .setStartEpochNanos(12345)
        .setEndEpochNanos(12349)
        .setAttributes(
            Attributes.builder()
                .put(KEY_BOOL, true)
                .put(KEY_STRING, "string")
                .put(KEY_INT, 100L)
                .put(KEY_DOUBLE, 100.3)
                .build())
        .setTotalAttributeCount(2)
        .setEvents(
            Arrays.asList(
                EventData.create(12347, "my_event_1", Attributes.empty()),
                EventData.create(12348, "my_event_2", Attributes.of(KEY_INT, 1234L)),
                EventData.create(12349, "my_event_3", Attributes.empty())))
        .setTotalRecordedEvents(4)
        .setLinks(
            Arrays.asList(
                LinkData.create(SPAN_CONTEXT),
                LinkData.create(SPAN_CONTEXT, Attributes.of(LINK_ATTR_KEY, "value"))))
        .setTotalRecordedLinks(3)
        .setStatus(StatusData.ok())
        .build();
  }

  @Test
  void validateOutput() throws Exception {
    byte[] result;
    {
      TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(spanDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeBinaryTo(customOutput);
      result = customOutput.toByteArray();
    }

    byte[] lowAllocationResult;
    {
      LowAllocationTraceRequestMarshaler requestMarshaler =
          new LowAllocationTraceRequestMarshaler();
      requestMarshaler.initialize(spanDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeBinaryTo(customOutput);
      lowAllocationResult = customOutput.toByteArray();
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }

  @Test
  void validateJsonOutput() throws Exception {
    String result;
    {
      TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(spanDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeJsonTo(customOutput);
      result = new String(customOutput.toByteArray(), StandardCharsets.UTF_8);
    }

    String lowAllocationResult;
    {
      LowAllocationTraceRequestMarshaler requestMarshaler =
          new LowAllocationTraceRequestMarshaler();
      requestMarshaler.initialize(spanDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeJsonTo(customOutput);
      lowAllocationResult = new String(customOutput.toByteArray(), StandardCharsets.UTF_8);
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }
}
