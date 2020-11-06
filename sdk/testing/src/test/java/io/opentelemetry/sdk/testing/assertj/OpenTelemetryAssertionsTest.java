/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PreferJavaTimeOverload")
class OpenTelemetryAssertionsTest {

  private static final String TRACE_ID = TraceId.fromLongs(1, 2);
  private static final String SPAN_ID1 = SpanId.fromLong(3);
  private static final String SPAN_ID2 = SpanId.fromLong(4);
  private static final TraceState TRACE_STATE = TraceState.builder().set("cat", "meow").build();
  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("dog", "bark").build());
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("opentelemetry", "1.0");
  private static final Attributes ATTRIBUTES =
      Attributes.builder()
          .put("bear", "mya")
          .put("warm", true)
          .put("temperature", 30)
          .put("length", 1.2)
          .put("colors", "red", "blue")
          .put("conditions", false, true)
          .put("scores", 0L, 1L)
          .put("coins", 0.01, 0.05, 0.1)
          .build();
  private static final List<SpanData.Event> EVENTS =
      Arrays.asList(
          SpanData.Event.create(10, "event", Attributes.empty()),
          SpanData.Event.create(
              20, "event2", Attributes.builder().put("cookie monster", "yum").build()));
  private static final List<SpanData.Link> LINKS =
      Arrays.asList(
          SpanData.Link.create(
              SpanContext.create(
                  TRACE_ID, SPAN_ID1, TraceFlags.getDefault(), TraceState.getDefault())),
          SpanData.Link.create(
              SpanContext.create(TRACE_ID, SPAN_ID2, TraceFlags.getSampled(), TRACE_STATE),
              Attributes.empty(),
              100));

  private static final TestSpanData SPAN1;
  private static final TestSpanData SPAN2;

  static {
    TestSpanData.Builder spanDataBuilder =
        TestSpanData.builder()
            .setTraceId(TRACE_ID)
            .setSpanId(SPAN_ID1)
            .setSampled(true)
            .setTraceState(TRACE_STATE)
            .setParentSpanId(SPAN_ID2)
            .setResource(RESOURCE)
            .setInstrumentationLibraryInfo(INSTRUMENTATION_LIBRARY_INFO)
            .setName("span")
            .setKind(Span.Kind.CLIENT)
            .setStartEpochNanos(100)
            .setAttributes(ATTRIBUTES)
            .setEvents(EVENTS)
            .setLinks(LINKS)
            .setStatus(SpanData.Status.ok())
            .setEndEpochNanos(200)
            .setHasRemoteParent(true)
            .setHasEnded(true)
            .setTotalRecordedEvents(300)
            .setTotalRecordedLinks(400)
            .setTotalAttributeCount(500);

    SPAN1 = spanDataBuilder.build();

    SPAN2 = spanDataBuilder.setSampled(false).setHasEnded(false).setHasRemoteParent(false).build();
  }

  @Test
  void passing() {
    assertThat(SPAN1)
        .hasTraceId(TRACE_ID)
        .hasSpanId(SPAN_ID1)
        .isSampled()
        .hasTraceState(TRACE_STATE)
        .hasParentSpanId(SPAN_ID2)
        .hasResource(RESOURCE)
        .hasInstrumentationLibraryInfo(INSTRUMENTATION_LIBRARY_INFO)
        .hasName("span")
        .hasKind(Span.Kind.CLIENT)
        .startsAt(100)
        .startsAt(100, TimeUnit.NANOSECONDS)
        .startsAt(Instant.ofEpochSecond(0, 100))
        .hasAttributes(ATTRIBUTES)
        .hasAttributesSatisfying(
            attributes ->
                assertThat(attributes)
                    .containsEntry(AttributeKey.stringKey("bear"), "mya")
                    .containsEntry("bear", "mya")
                    .containsEntry("warm", true)
                    .containsEntry("temperature", 30)
                    .containsEntry("length", 1.2)
                    .containsEntry("colors", "red", "blue")
                    .containsEntryWithStringValuesOf("colors", Arrays.asList("red", "blue"))
                    .containsEntry("conditions", false, true)
                    .containsEntryWithBooleanValuesOf("conditions", Arrays.asList(false, true))
                    .containsEntry("scores", 0L, 1L)
                    .containsEntryWithLongValuesOf("scores", Arrays.asList(0L, 1L))
                    .containsEntry("coins", 0.01, 0.05, 0.1)
                    .containsEntryWithDoubleValuesOf("coins", Arrays.asList(0.01, 0.05, 0.1)))
        .hasEvents(EVENTS)
        .hasEvents(EVENTS.toArray(new SpanData.Event[0]))
        .hasEventsSatisfying(events -> assertThat(events).hasSize(EVENTS.size()))
        .hasLinks(LINKS)
        .hasLinks(LINKS.toArray(new SpanData.Link[0]))
        .hasLinksSatisfying(links -> assertThat(links).hasSize(LINKS.size()))
        .hasStatus(SpanData.Status.ok())
        .endsAt(200)
        .endsAt(200, TimeUnit.NANOSECONDS)
        .endsAt(Instant.ofEpochSecond(0, 200))
        .hasRemoteParent()
        .hasEnded()
        .hasTotalRecordedEvents(300)
        .hasTotalRecordedLinks(400)
        .hasTotalAttributeCount(500);

    assertThat(SPAN2).isNotSampled().hasNotEnded().doesNotHaveRemoteParent();
  }

  @Test
  void failure() {
    assertThatThrownBy(() -> assertThat(SPAN1).hasTraceId("foo"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasSpanId("foo")).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).isNotSampled()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasTraceState(TraceState.getDefault()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasParentSpanId("foo"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasResource(Resource.getEmpty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasInstrumentationLibraryInfo(InstrumentationLibraryInfo.getEmpty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasName("foo")).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasKind(Span.Kind.SERVER))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(10)).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(10, TimeUnit.NANOSECONDS))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(Instant.EPOCH))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasAttributes(Attributes.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfying(
                        attributes -> assertThat(attributes).containsEntry("cat", "bark")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasEvents()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasEvents(Collections.emptyList()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(SPAN1).hasEventsSatisfying(events -> assertThat(events).isEmpty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasLinks()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasLinks(Collections.emptyList()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(SPAN1).hasLinksSatisfying(links -> assertThat(links).isEmpty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasStatus(SpanData.Status.error()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).endsAt(10)).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).endsAt(10, TimeUnit.NANOSECONDS))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).endsAt(Instant.EPOCH))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).doesNotHaveRemoteParent())
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasNotEnded()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasTotalRecordedEvents(1))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasTotalRecordedLinks(1))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasTotalAttributeCount(1))
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> assertThat(SPAN2).isSampled()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN2).hasEnded()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN2).hasRemoteParent())
        .isInstanceOf(AssertionError.class);
  }
}
