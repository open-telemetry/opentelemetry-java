/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

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
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PreferJavaTimeOverload")
class OpenTelemetryAssertionsTest {
  private static final String TRACE_ID = "00000000000000010000000000000002";
  private static final String SPAN_ID1 = "0000000000000003";
  private static final String SPAN_ID2 = "0000000000000004";
  private static final TraceState TRACE_STATE = TraceState.builder().put("cat", "meow").build();
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
  private static final List<EventData> EVENTS =
      Arrays.asList(
          EventData.create(10, "event", Attributes.empty()),
          EventData.create(20, "event2", Attributes.builder().put("cookie monster", "yum").build()),
          EventData.create(
              30,
              SemanticAttributes.EXCEPTION_EVENT_NAME,
              Attributes.builder()
                  .put(SemanticAttributes.EXCEPTION_TYPE, "java.lang.IllegalArgumentException")
                  .put(SemanticAttributes.EXCEPTION_MESSAGE, "bad argument")
                  .put(SemanticAttributes.EXCEPTION_STACKTRACE, "some obfuscated stack")
                  .build()));
  private static final List<LinkData> LINKS =
      Arrays.asList(
          LinkData.create(
              SpanContext.create(
                  TRACE_ID, SPAN_ID1, TraceFlags.getDefault(), TraceState.getDefault())),
          LinkData.create(
              SpanContext.create(TRACE_ID, SPAN_ID2, TraceFlags.getSampled(), TRACE_STATE),
              Attributes.empty(),
              100));

  private static final TestSpanData SPAN1;
  private static final TestSpanData SPAN2;

  static {
    TestSpanData.Builder spanDataBuilder =
        TestSpanData.builder()
            .setParentSpanContext(
                SpanContext.create(
                    TRACE_ID, SPAN_ID2, TraceFlags.getDefault(), TraceState.getDefault()))
            .setResource(RESOURCE)
            .setInstrumentationLibraryInfo(INSTRUMENTATION_LIBRARY_INFO)
            .setName("span")
            .setKind(SpanKind.CLIENT)
            .setStartEpochNanos(100)
            .setAttributes(ATTRIBUTES)
            .setEvents(EVENTS)
            .setLinks(LINKS)
            .setStatus(StatusData.ok())
            .setEndEpochNanos(200)
            .setHasEnded(true)
            .setTotalRecordedEvents(300)
            .setTotalRecordedLinks(400)
            .setTotalAttributeCount(500);

    SPAN1 =
        spanDataBuilder
            .setSpanContext(
                SpanContext.create(TRACE_ID, SPAN_ID1, TraceFlags.getSampled(), TRACE_STATE))
            .build();

    SPAN2 =
        spanDataBuilder
            .setSpanContext(
                SpanContext.create(TRACE_ID, SPAN_ID1, TraceFlags.getDefault(), TRACE_STATE))
            .setHasEnded(false)
            .build();
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
        .hasKind(SpanKind.CLIENT)
        .startsAt(100)
        .startsAt(100, TimeUnit.NANOSECONDS)
        .startsAt(Instant.ofEpochSecond(0, 100))
        .hasAttributes(ATTRIBUTES)
        .hasAttributes(
            attributeEntry("bear", "mya"),
            attributeEntry("warm", true),
            attributeEntry("temperature", 30),
            attributeEntry("length", 1.2),
            attributeEntry("colors", "red", "blue"),
            attributeEntry("conditions", false, true),
            attributeEntry("scores", 0L, 1L),
            attributeEntry("coins", 0.01, 0.05, 0.1))
        .hasAttributesSatisfying(
            attributes ->
                assertThat(attributes)
                    .hasSize(8)
                    .containsEntry(AttributeKey.stringKey("bear"), "mya")
                    .hasEntrySatisfying(
                        AttributeKey.stringKey("bear"), value -> assertThat(value).hasSize(3))
                    .containsEntry("bear", "mya")
                    .containsEntry("warm", true)
                    .containsEntry("temperature", 30)
                    .containsEntry(AttributeKey.longKey("temperature"), 30L)
                    .containsEntry(AttributeKey.longKey("temperature"), 30)
                    .containsEntry("length", 1.2)
                    .containsEntry("colors", "red", "blue")
                    .containsEntryWithStringValuesOf("colors", Arrays.asList("red", "blue"))
                    .containsEntry("conditions", false, true)
                    .containsEntryWithBooleanValuesOf("conditions", Arrays.asList(false, true))
                    .containsEntry("scores", 0L, 1L)
                    .containsEntryWithLongValuesOf("scores", Arrays.asList(0L, 1L))
                    .containsEntry("coins", 0.01, 0.05, 0.1)
                    .containsEntryWithDoubleValuesOf("coins", Arrays.asList(0.01, 0.05, 0.1))
                    .containsKey(AttributeKey.stringKey("bear"))
                    .containsKey("bear")
                    .containsOnly(
                        attributeEntry("bear", "mya"),
                        attributeEntry("warm", true),
                        attributeEntry("temperature", 30),
                        attributeEntry("length", 1.2),
                        attributeEntry("colors", "red", "blue"),
                        attributeEntry("conditions", false, true),
                        attributeEntry("scores", 0L, 1L),
                        attributeEntry("coins", 0.01, 0.05, 0.1)))
        .hasEvents(EVENTS)
        .hasEvents(EVENTS.toArray(new EventData[0]))
        .hasEventsSatisfying(
            events -> {
              assertThat(events).hasSize(EVENTS.size());
              assertThat(events.get(0))
                  .hasName("event")
                  .hasTimestamp(10)
                  .hasTimestamp(10, TimeUnit.NANOSECONDS)
                  .hasTimestamp(Instant.ofEpochSecond(0, 10))
                  .hasAttributes(Attributes.empty())
                  .hasAttributesSatisfying(
                      attributes -> assertThat(attributes).isEqualTo(Attributes.empty()))
                  .hasAttributesSatisfying(attributes -> assertThat(attributes).isEmpty());
            })
        .hasEventsSatisfyingExactly(
            event -> event.hasName("event"),
            event -> event.hasName("event2"),
            event -> event.hasName(SemanticAttributes.EXCEPTION_EVENT_NAME))
        .hasException(new IllegalArgumentException("bad argument"))
        .hasLinks(LINKS)
        .hasLinks(LINKS.toArray(new LinkData[0]))
        .hasLinksSatisfying(links -> assertThat(links).hasSize(LINKS.size()))
        .hasStatus(StatusData.ok())
        .endsAt(200)
        .endsAt(200, TimeUnit.NANOSECONDS)
        .endsAt(Instant.ofEpochSecond(0, 200))
        .hasEnded()
        .hasTotalRecordedEvents(300)
        .hasTotalRecordedLinks(400)
        .hasTotalAttributeCount(500);

    assertThat(RESOURCE.getAttributes()).containsOnly(entry(AttributeKey.stringKey("dog"), "bark"));
  }

  @Test
  void failure() {
    assertThatThrownBy(() -> assertThat(SPAN1).hasTraceId("foo"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasSpanId("foo")).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).isNotSampled()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasTraceState(TraceState.getDefault()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasNoParent()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasParentSpanId("foo"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasResource(Resource.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1).hasInstrumentationLibraryInfo(InstrumentationLibraryInfo.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasName("foo")).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasKind(SpanKind.SERVER))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(10)).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(10, TimeUnit.NANOSECONDS))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(Instant.EPOCH))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasAttributes(Attributes.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasAttributes(attributeEntry("food", "burger")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfying(
                        attributes -> assertThat(attributes).containsEntry("cat", "bark")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfying(
                        attributes ->
                            assertThat(attributes).containsKey(AttributeKey.stringKey("cat"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfying(
                        attributes -> assertThat(attributes).containsKey("cat")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfying(attributes -> assertThat(attributes).isEmpty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfying(attributes -> assertThat(attributes).hasSize(33)))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfying(
                        attributes ->
                            assertThat(attributes)
                                .hasEntrySatisfying(
                                    AttributeKey.stringKey("bear"),
                                    value -> assertThat(value).hasSize(2))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasEvents()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasEvents(Collections.emptyList()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
        () -> assertThat(SPAN1).hasEventsSatisfying(events -> assertThat(events).isEmpty()));
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasEventsSatisfying(events -> assertThat(events.get(0)).hasName("notevent")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(SPAN1).hasEventsSatisfyingExactly(event -> event.hasName("notevent")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasEventsSatisfying(events -> assertThat(events.get(0)).hasTimestamp(1)))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasEventsSatisfying(
                        events -> assertThat(events.get(0)).hasTimestamp(1, TimeUnit.NANOSECONDS)))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasEventsSatisfying(
                        events ->
                            assertThat(events.get(0)).hasTimestamp(Instant.ofEpochSecond(0, 1))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasEventsSatisfying(
                        events ->
                            assertThat(events.get(0)).hasAttributes(RESOURCE.getAttributes())))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasEventsSatisfying(
                        events ->
                            assertThat(events.get(0))
                                .hasAttributesSatisfying(
                                    attributes ->
                                        assertThat(attributes).containsEntry("dogs", "meow"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(SPAN1).hasException(new IllegalStateException("bad argument")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(SPAN1).hasException(new IllegalArgumentException("good argument")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasLinks()).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasLinks(Collections.emptyList()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(SPAN1).hasLinksSatisfying(links -> assertThat(links).isEmpty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasStatus(StatusData.error()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).endsAt(10)).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).endsAt(10, TimeUnit.NANOSECONDS))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).endsAt(Instant.EPOCH))
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

    assertThatThrownBy(
            () ->
                assertThat(RESOURCE.getAttributes())
                    .containsOnly(
                        entry(AttributeKey.stringKey("dog"), "bark"),
                        entry(AttributeKey.stringKey("cat"), "meow")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(RESOURCE.getAttributes())
                    .containsOnly(entry(AttributeKey.stringKey("cat"), "meow")))
        .isInstanceOf(AssertionError.class);
  }
}
