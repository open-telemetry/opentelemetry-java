/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.json;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Collections;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class JsonSpanAdapterTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());

  private static final JsonSpanAdapter.ThreadLocalCache threadLocalCache =
      new JsonSpanAdapter.ThreadLocalCache();

  @Test
  void toProtoResourceSpans() {
    JSONArray resourceSpans =
        JsonSpanAdapter.toJsonResourceSpans(
            Collections.singleton(
                TestSpanData.builder()
                    .setHasEnded(true)
                    .setSpanContext(SPAN_CONTEXT)
                    .setParentSpanContext(SpanContext.getInvalid())
                    .setName("GET /api/endpoint")
                    .setKind(SpanKind.SERVER)
                    .setStartEpochNanos(12345)
                    .setEndEpochNanos(12349)
                    .setStatus(StatusData.unset())
                    .setInstrumentationLibraryInfo(
                        InstrumentationLibraryInfo.create("testLib", "1.0", "http://url"))
                    .setResource(
                        Resource.builder().put("one", 1).setSchemaUrl("http://url").build())
                    .build()));

    assertThat(resourceSpans).hasSize(1);
    JSONObject onlyResourceSpans = (JSONObject) resourceSpans.get(0);
    assertThat(onlyResourceSpans.get("schema_url")).isEqualTo("http://url");

    JSONArray instrumentationLibrarySpans =
        (JSONArray) onlyResourceSpans.get("instrumentation_library_spans");
    assertThat(instrumentationLibrarySpans.size()).isEqualTo(1);

    JSONObject instrumentationLibrarySpan = (JSONObject) instrumentationLibrarySpans.get(0);
    assertThat(instrumentationLibrarySpan.get("schema_url")).isEqualTo("http://url");

    JSONObject instrumentationLibrary = new JSONObject();
    instrumentationLibrary.put("name", "testLib");
    instrumentationLibrary.put("version", "1.0");
    assertThat(instrumentationLibrarySpan.get("instrumentation_library"))
        .isEqualTo(instrumentationLibrary);
  }

  // Repeat to reuse the cache. If we forgot to clear any reused builder it will fail.
  @RepeatedTest(3)
  void toProtoSpan() {
    JSONObject span =
        JsonSpanAdapter.toProtoSpan(
            TestSpanData.builder()
                .setHasEnded(true)
                .setSpanContext(SPAN_CONTEXT)
                .setParentSpanContext(SpanContext.getInvalid())
                .setName("GET /api/endpoint")
                .setKind(SpanKind.SERVER)
                .setStartEpochNanos(12345)
                .setEndEpochNanos(12349)
                .setAttributes(Attributes.of(booleanKey("key"), true))
                .setTotalAttributeCount(2)
                .setEvents(
                    Collections.singletonList(
                        EventData.create(12347, "my_event", Attributes.empty())))
                .setTotalRecordedEvents(3)
                .setLinks(Collections.singletonList(LinkData.create(SPAN_CONTEXT)))
                .setTotalRecordedLinks(2)
                .setStatus(StatusData.ok())
                .build(),
            threadLocalCache);

    assertThat(span.get("trace_id")).isEqualTo(TRACE_ID);
    assertThat(span.get("span_id")).isEqualTo(SPAN_ID);
    assertThat(span.get("parent_span_id")).isNull();
    assertThat(span.get("name")).isEqualTo("GET /api/endpoint");
    assertThat(span.get("kind")).isEqualTo("SPAN_KIND_SERVER");
    assertThat(span.get("start_time_unix_nano")).isEqualTo(12345L);
    assertThat(span.get("end_time_unix_nano")).isEqualTo(12349L);
    JSONArray attributes = (JSONArray) span.get("attributes");
    assertThat(attributes.size()).isEqualTo(1);
    assertThat(span.get("dropped_attributes_count")).isEqualTo(1);
    JSONArray events = (JSONArray) span.get("events");
    assertThat(events.size()).isEqualTo(1);
    assertThat(span.get("dropped_events_count")).isEqualTo(2); // 3 - 1
    JSONArray links = (JSONArray) span.get("links");
    assertThat(links.size()).isEqualTo(1);
    assertThat(span.get("dropped_links_count")).isEqualTo(1); // 2 - 1
    JSONObject statusOk = new JSONObject();
    statusOk.put("code", "STATUS_CODE_OK");
    statusOk.put("deprecated_code", "DEPRECATED_STATUS_CODE_OK");
    statusOk.put("message", "OK_OVERRIDE");
    assertThat(span.get("status")).isEqualTo(statusOk);
  }

  @Test
  void toProtoSpanKind() {
    assertThat(JsonSpanAdapter.toProtoSpanKind(SpanKind.INTERNAL)).isEqualTo("SPAN_KIND_INTERNAL");
    assertThat(JsonSpanAdapter.toProtoSpanKind(SpanKind.CLIENT)).isEqualTo("SPAN_KIND_CLIENT");
    assertThat(JsonSpanAdapter.toProtoSpanKind(SpanKind.SERVER)).isEqualTo("SPAN_KIND_SERVER");
    assertThat(JsonSpanAdapter.toProtoSpanKind(SpanKind.PRODUCER)).isEqualTo("SPAN_KIND_PRODUCER");
    assertThat(JsonSpanAdapter.toProtoSpanKind(SpanKind.CONSUMER)).isEqualTo("SPAN_KIND_CONSUMER");
  }

  @Test
  void toProtoStatus() {
    JSONObject statusCodeUnset = new JSONObject();
    statusCodeUnset.put("code", "STATUS_CODE_UNSET");
    statusCodeUnset.put("deprecated_code", "DEPRECATED_STATUS_CODE_OK");
    assertThat(JsonSpanAdapter.toStatusProto(StatusData.unset())).isEqualTo(statusCodeUnset);

    JSONObject statusCodeError = new JSONObject();
    statusCodeError.put("code", "STATUS_CODE_ERROR");
    statusCodeError.put("deprecated_code", "DEPRECATED_STATUS_CODE_UNKNOWN_ERROR");
    statusCodeError.put("message", "ERROR");
    assertThat(JsonSpanAdapter.toStatusProto(StatusData.create(StatusCode.ERROR, "ERROR")))
        .isEqualTo(statusCodeError);

    JSONObject statusCodeUnKnown = new JSONObject();
    statusCodeUnKnown.put("code", "STATUS_CODE_ERROR");
    statusCodeUnKnown.put("deprecated_code", "DEPRECATED_STATUS_CODE_UNKNOWN_ERROR");
    statusCodeUnKnown.put("message", "UNKNOWN");
    assertThat(JsonSpanAdapter.toStatusProto(StatusData.create(StatusCode.ERROR, "UNKNOWN")))
        .isEqualTo(statusCodeUnKnown);

    JSONObject statusCodeOkOverride = new JSONObject();
    statusCodeOkOverride.put("code", "STATUS_CODE_OK");
    statusCodeOkOverride.put("deprecated_code", "DEPRECATED_STATUS_CODE_OK");
    statusCodeOkOverride.put("message", "OK_OVERRIDE");
    assertThat(JsonSpanAdapter.toStatusProto(StatusData.create(StatusCode.OK, "OK_OVERRIDE")))
        .isEqualTo(statusCodeOkOverride);
  }

  @Test
  void toProtoSpanEvent_WithoutAttributes() {
    JSONObject spanEvent = new JSONObject();
    spanEvent.put("time_unix_nano", 12345L);
    spanEvent.put("name", "test_without_attributes");
    spanEvent.put("attributes", new JSONArray());
    spanEvent.put("dropped_attributes_count", 0);
    assertThat(
            JsonSpanAdapter.toProtoSpanEvent(
                EventData.create(12345, "test_without_attributes", Attributes.empty()),
                threadLocalCache))
        .isEqualTo(spanEvent);
  }

  @Test
  void toProtoSpanEvent_WithAttributes() {
    JSONObject spanEvent = new JSONObject();
    spanEvent.put("time_unix_nano", 12345L);
    spanEvent.put("name", "test_with_attributes");
    JSONArray attributes = new JSONArray();
    JSONObject attribute = new JSONObject();
    attribute.put("key", "key_string");
    JSONObject stringValue = new JSONObject();
    stringValue.put("string_value", "string");
    attribute.put("value", stringValue);
    attributes.add(attribute);
    spanEvent.put("attributes", attributes);
    spanEvent.put("dropped_attributes_count", 4);

    assertThat(
            JsonSpanAdapter.toProtoSpanEvent(
                EventData.create(
                    12345,
                    "test_with_attributes",
                    Attributes.of(stringKey("key_string"), "string"),
                    5),
                threadLocalCache))
        .isEqualTo(spanEvent);
  }

  @Test
  void toProtoSpanLink_WithoutAttributes() {
    JSONObject spanLink = new JSONObject();
    spanLink.put("trace_id", TRACE_ID);
    spanLink.put("span_id", SPAN_ID);
    spanLink.put("attributes", new JSONArray());
    spanLink.put("dropped_attributes_count", 0);
    assertThat(JsonSpanAdapter.toProtoSpanLink(LinkData.create(SPAN_CONTEXT), threadLocalCache))
        .isEqualTo(spanLink);
  }

  @Test
  void toProtoSpanLink_WithAttributes() {
    JSONObject spanLink = new JSONObject();
    spanLink.put("trace_id", TRACE_ID);
    spanLink.put("span_id", SPAN_ID);
    JSONArray attributes = new JSONArray();
    JSONObject attribute = new JSONObject();
    attribute.put("key", "key_string");
    JSONObject stringValue = new JSONObject();
    stringValue.put("string_value", "string");
    attribute.put("value", stringValue);
    attributes.add(attribute);
    spanLink.put("attributes", attributes);
    spanLink.put("dropped_attributes_count", 4);
    assertThat(
            JsonSpanAdapter.toProtoSpanLink(
                LinkData.create(SPAN_CONTEXT, Attributes.of(stringKey("key_string"), "string"), 5),
                threadLocalCache))
        .isEqualTo(spanLink);
  }
}
