/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
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
import java.util.List;
import org.junit.jupiter.api.Test;

class SpanMarshalerTest {

  @Test
  void marshalToJson() throws Exception {
    String traceId = "abcabcabcabcabcabcabcabcabcabcab";
    String spanId = "1234123412341234";
    SpanContext spanContext =
        SpanContext.create(traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());
    long now = 1706048791269000000L;
    long later = 1706048791274000000L;
    EventData event = EventData.create(now + 42L, "reboot", Attributes.of(stringKey("e1"), "v1"));
    List<EventData> events = singletonList(event);
    LinkData link = LinkData.create(spanContext, Attributes.of(stringKey("link1"), "lv1"));
    List<LinkData> links = singletonList(link);
    SpanData spanData =
        TestSpanData.builder()
            .setHasEnded(true)
            .setSpanContext(spanContext)
            .setParentSpanContext(SpanContext.getInvalid())
            .setName("testSpan")
            .setKind(SpanKind.SERVER)
            .setStartEpochNanos(now)
            .setEndEpochNanos(later)
            .setStatus(StatusData.create(StatusCode.OK, "radical"))
            .setAttributes(Attributes.of(stringKey("flim"), "flam"))
            .setInstrumentationScopeInfo(
                InstrumentationScopeInfo.builder("testInst")
                    .setVersion("1.0")
                    .setSchemaUrl("http://url")
                    .setAttributes(Attributes.builder().put("foo", "bar").build())
                    .build())
            .setResource(Resource.builder().put("rez", 42).setSchemaUrl("http://url").build())
            .setEvents(events)
            .setLinks(links)
            .build();
    SpanMarshaler spanMarshaler = SpanMarshaler.create(spanData);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    spanMarshaler.writeJsonTo(out);
    String result = out.toString(StandardCharsets.UTF_8.name());

    String expected =
        "{\"traceId\":\"abcabcabcabcabcabcabcabcabcabcab\",\"spanId\":\"1234123412341234\","
            + "\"name\":\"testSpan\",\"kind\":2,"
            + "\"startTimeUnixNano\":\"1706048791269000000\",\"endTimeUnixNano\":\"1706048791274000000\","
            + "\"attributes\":["
            + "{\"key\":\"flim\",\"value\":{\"stringValue\":\"flam\"}}"
            + "],"
            + "\"droppedAttributesCount\":-1,"
            + "\"events\":[{\"timeUnixNano\":\"1706048791269000042\",\"name\":\"reboot\",\"attributes\":[{\"key\":\"e1\",\"value\":{\"stringValue\":\"v1\"}}]}],\"droppedEventsCount\":-1,"
            + "\"links\":[{\"traceId\":\"abcabcabcabcabcabcabcabcabcabcab\",\"spanId\":\"1234123412341234\",\"attributes\":[{\"key\":\"link1\",\"value\":{\"stringValue\":\"lv1\"}}],\"flags\":1}],\"droppedLinksCount\":-1,"
            + "\"status\":{\"message\":\"radical\",\"code\":1},"
            + "\"flags\":1}";
    assertThat(result).isEqualTo(expected);
  }
}
