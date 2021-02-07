/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class RequestMarshalState {
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
              .build());

  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("name", null);
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {123, 46, 23, 78, 12, 5, (byte) 180, (byte) 223, 45, 89, 71, 61, 62, 29, 34, 54};
  private static final String TRACE_ID = TraceId.bytesToHex(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES =
      new byte[] {(byte) 198, (byte) 245, (byte) 213, (byte) 156, 46, 31, 29, 101};
  private static final String SPAN_ID = SpanId.bytesToHex(SPAN_ID_BYTES);

  private static final TraceState TRACE_STATE = TraceState.builder().build();
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TRACE_STATE);

  @Param({"16"})
  int numSpans;

  List<SpanData> spanDataList;

  @Setup
  public void setup() {
    spanDataList = new ArrayList<>(numSpans);
    for (int i = 0; i < numSpans; i++) {
      spanDataList.add(createSpanData());
    }
  }

  private static SpanData createSpanData() {
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
                    Attributes.of(AttributeKey.longKey("event_attr_key"), 1234L)),
                EventData.create(12349, "my_event_3", Attributes.empty())))
        .setTotalRecordedEvents(4)
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
