/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
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
              .build());

  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("name", null);
  private static final String TRACE_ID = "7b2e170db4df2d593ddb4ddf2ddf2d59";
  private static final String SPAN_ID = "170d3ddb4d23e81f";
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());

  @Param({"16", "512"})
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
}
