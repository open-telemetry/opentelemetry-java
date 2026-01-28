/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.common.AttributeKey.valueKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.offset;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
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
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PreferJavaTimeOverload")
class TraceAssertionsTest {
  private static final String TRACE_ID = "00000000000000010000000000000002";
  private static final String SPAN_ID1 = "0000000000000003";
  private static final String SPAN_ID2 = "0000000000000004";
  private static final TraceState TRACE_STATE = TraceState.builder().put("cat", "meow").build();
  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("dog", "bark").put("dog is cute", true).build());
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.builder("opentelemetry").setVersion("1.0").build();

  private static final AttributeKey<String> DOG = stringKey("dog");
  private static final AttributeKey<String> BEAR = stringKey("bear");
  private static final AttributeKey<String> CAT = stringKey("cat");
  private static final AttributeKey<Boolean> WARM = AttributeKey.booleanKey("warm");
  private static final AttributeKey<Long> TEMPERATURE = AttributeKey.longKey("temperature");
  private static final AttributeKey<Double> LENGTH = AttributeKey.doubleKey("length");
  private static final AttributeKey<List<String>> COLORS = AttributeKey.stringArrayKey("colors");
  private static final AttributeKey<List<Boolean>> CONDITIONS =
      AttributeKey.booleanArrayKey("conditions");
  private static final AttributeKey<List<Long>> SCORES = AttributeKey.longArrayKey("scores");
  private static final AttributeKey<List<Double>> COINS = AttributeKey.doubleArrayKey("coins");
  private static final AttributeKey<Value<?>> BYTES = valueKey("bytes");
  private static final AttributeKey<String> UNSET = stringKey("unset");

  private static final Attributes ATTRIBUTES =
      Attributes.builder()
          .put(BEAR, "mya")
          .put(WARM, true)
          .put(TEMPERATURE, 30)
          .put(LENGTH, 1.2)
          .put(COLORS, Arrays.asList("red", "blue"))
          .put(CONDITIONS, Arrays.asList(false, true))
          .put(SCORES, Arrays.asList(0L, 1L))
          .put(COINS, Arrays.asList(0.01, 0.05, 0.1))
          .put(BYTES, Value.of(new byte[] {1, 2, 3}))
          .build();
  private static final List<EventData> EVENTS =
      Arrays.asList(
          EventData.create(10, "event", Attributes.empty()),
          EventData.create(20, "event2", Attributes.builder().put("cookie monster", "yum").build()),
          EventData.create(
              30,
              "exception",
              Attributes.builder()
                  .put(stringKey("exception.type"), "java.lang.IllegalArgumentException")
                  .put(stringKey("exception.message"), "bad argument")
                  .put(stringKey("exception.stacktrace"), "some obfuscated stack")
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
            .setInstrumentationScopeInfo(INSTRUMENTATION_SCOPE_INFO)
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
  @SuppressWarnings("Convert2MethodRef")
  void passing() {
    assertThat(SPAN1)
        .hasTraceId(TRACE_ID)
        .hasSpanId(SPAN_ID1)
        .isSampled()
        .hasTraceState(TRACE_STATE)
        .hasParentSpanId(SPAN_ID2)
        .hasResource(RESOURCE)
        .hasResourceSatisfying(
            resource ->
                resource
                    .hasSchemaUrl(null)
                    .hasAttribute(DOG, "bark")
                    .hasAttributes(
                        Attributes.of(DOG, "bark", AttributeKey.booleanKey("dog is cute"), true))
                    .hasAttributes(
                        attributeEntry("dog", "bark"), attributeEntry("dog is cute", true))
                    .hasAttributesSatisfying(
                        attributes ->
                            assertThat(attributes)
                                .hasSize(2)
                                .containsEntry(stringKey("dog"), "bark")
                                .hasEntrySatisfying(DOG, value -> assertThat(value).hasSize(4))
                                .hasEntrySatisfying(
                                    AttributeKey.booleanKey("dog is cute"),
                                    value -> assertThat(value).isTrue())))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfying(satisfies(DOG, val -> val.isEqualTo("bark"))))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfyingExactly(
                    equalTo(DOG, "bark"), equalTo(AttributeKey.booleanKey("dog is cute"), true)))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfyingExactly(
                    satisfies(DOG, val -> val.startsWith("bar")),
                    satisfies(AttributeKey.booleanKey("dog is cute"), val -> val.isTrue())))
        .hasInstrumentationScopeInfo(INSTRUMENTATION_SCOPE_INFO)
        .hasName("span")
        .hasKind(SpanKind.CLIENT)
        .startsAt(100)
        .startsAt(100, TimeUnit.NANOSECONDS)
        .startsAt(Instant.ofEpochSecond(0, 100))
        .hasAttribute(BEAR, "mya")
        .hasAttribute(equalTo(BEAR, "mya"))
        .hasAttributes(ATTRIBUTES)
        .hasAttributes(
            attributeEntry("bear", "mya"),
            attributeEntry("warm", true),
            attributeEntry("temperature", 30),
            attributeEntry("length", 1.2),
            attributeEntry("colors", "red", "blue"),
            attributeEntry("conditions", false, true),
            attributeEntry("scores", 0L, 1L),
            attributeEntry("coins", 0.01, 0.05, 0.1),
            attributeEntry("bytes", Value.of(new byte[] {1, 2, 3})))
        .hasAttributesSatisfying(
            equalTo(BEAR, "mya"), equalTo(WARM, true), equalTo(TEMPERATURE, 30))
        .hasAttributesSatisfyingExactly(
            equalTo(BEAR, "mya"),
            equalTo(WARM, true),
            equalTo(TEMPERATURE, 30),
            equalTo(LENGTH, 1.2),
            equalTo(COLORS, Arrays.asList("red", "blue")),
            equalTo(CONDITIONS, Arrays.asList(false, true)),
            equalTo(SCORES, Arrays.asList(0L, 1L)),
            equalTo(COINS, Arrays.asList(0.01, 0.05, 0.1)),
            equalTo(BYTES, Value.of(new byte[] {1, 2, 3})))
        .hasAttributesSatisfyingExactly(
            satisfies(BEAR, val -> val.startsWith("mya")),
            satisfies(WARM, val -> val.isTrue()),
            satisfies(TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
            satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
            satisfies(COLORS, val -> val.containsExactly("red", "blue")),
            satisfies(CONDITIONS, val -> val.containsExactly(false, true)),
            satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
            satisfies(COINS, val -> val.containsExactly(0.01, 0.05, 0.1)),
            satisfies(BYTES, val -> val.isEqualTo(Value.of(new byte[] {1, 2, 3}))))
        // Demonstrates common usage of many exact matches and one needing a loose one.
        .hasAttributesSatisfyingExactly(
            equalTo(BEAR, "mya"),
            equalTo(WARM, true),
            equalTo(TEMPERATURE, 30L),
            equalTo(COLORS, Arrays.asList("red", "blue")),
            equalTo(CONDITIONS, Arrays.asList(false, true)),
            equalTo(SCORES, Arrays.asList(0L, 1L)),
            equalTo(COINS, Arrays.asList(0.01, 0.05, 0.1)),
            equalTo(BYTES, Value.of(new byte[] {1, 2, 3})),
            satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))))
        .hasAttributesSatisfying(
            attributes ->
                assertThat(attributes)
                    .hasSize(9)
                    .containsEntry(stringKey("bear"), "mya")
                    .hasEntrySatisfying(stringKey("bear"), value -> assertThat(value).hasSize(3))
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
                    .containsEntry("bytes", Value.of(new byte[] {1, 2, 3}))
                    .containsKey(stringKey("bear"))
                    .containsKey("bear")
                    .doesNotContainKey(stringKey("cat"))
                    .doesNotContainKey("cat")
                    .containsOnly(
                        attributeEntry("bear", "mya"),
                        attributeEntry("warm", true),
                        attributeEntry("temperature", 30),
                        attributeEntry("length", 1.2),
                        attributeEntry("colors", "red", "blue"),
                        attributeEntry("conditions", false, true),
                        attributeEntry("scores", 0L, 1L),
                        attributeEntry("coins", 0.01, 0.05, 0.1),
                        attributeEntry("bytes", Value.of(new byte[] {1, 2, 3}))))
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
              assertThat(events.get(2))
                  .hasAttributesSatisfying(
                      equalTo(stringKey("exception.type"), "java.lang.IllegalArgumentException"))
                  .hasAttributesSatisfyingExactly(
                      equalTo(stringKey("exception.type"), "java.lang.IllegalArgumentException"),
                      equalTo(stringKey("exception.message"), "bad argument"),
                      equalTo(stringKey("exception.stacktrace"), "some obfuscated stack"));
            })
        .hasEventsSatisfyingExactly(
            event -> event.hasName("event"),
            event -> event.hasName("event2"),
            event -> event.hasName("exception"))
        .hasException(new IllegalArgumentException("bad argument"))
        .hasLinks(LINKS)
        .hasLinks(LINKS.toArray(new LinkData[0]))
        .hasLinksSatisfying(links -> assertThat(links).hasSize(LINKS.size()))
        .hasStatus(StatusData.ok())
        .hasStatusSatisfying(
            status ->
                status
                    .isOk()
                    .hasCode(StatusCode.OK)
                    .hasDescription("")
                    .hasDescriptionMatching("^$"))
        .endsAt(200)
        .endsAt(200, TimeUnit.NANOSECONDS)
        .endsAt(Instant.ofEpochSecond(0, 200))
        .hasEnded()
        .hasTotalRecordedEvents(300)
        .hasTotalRecordedLinks(400)
        .hasTotalAttributeCount(500);

    assertThat(RESOURCE.getAttributes())
        .containsOnly(entry(DOG, "bark"), entry(AttributeKey.booleanKey("dog is cute"), true));
  }

  @Test
  @SuppressWarnings("Convert2MethodRef")
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
                assertThat(SPAN1)
                    .hasResourceSatisfying(resource -> resource.hasSchemaUrl("http://example.com")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(resource -> resource.hasAttribute(DOG, "meow")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(
                        resource -> resource.hasAttributes(Attributes.of(DOG, "bark"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(
                        resource -> resource.hasAttributes(attributeEntry("dog is cute", true))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes -> assertThat(attributes).hasSize(1))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes ->
                                    assertThat(attributes)
                                        .containsEntry(stringKey("dog"), "meow"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes ->
                                    assertThat(attributes)
                                        .containsEntry(
                                            AttributeKey.booleanKey("dog is cute"), false))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                satisfies(DOG, val -> val.isEqualTo("meow")))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(
                        resource -> resource.hasAttributesSatisfyingExactly(equalTo(DOG, "bark"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfyingExactly(
                                satisfies(DOG, val -> val.isEqualTo("bark")))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(SPAN1).hasInstrumentationScopeInfo(InstrumentationScopeInfo.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasName("foo")).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasKind(SpanKind.SERVER))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(10)).isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(10, TimeUnit.NANOSECONDS))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).startsAt(Instant.EPOCH))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasAttribute(stringKey("foo"), "bar"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasAttributes(Attributes.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(SPAN1).hasAttributes(attributeEntry("food", "burger")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfyingExactly(
                        // Failed
                        satisfies(BEAR, val -> val.doesNotContain("mya")),
                        satisfies(WARM, val -> val.isTrue()),
                        satisfies(TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
                        satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                        satisfies(COLORS, val -> val.containsExactly("red", "blue")),
                        satisfies(CONDITIONS, val -> val.containsExactly(false, true)),
                        satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
                        satisfies(COINS, val -> val.containsExactly(0.01, 0.05, 0.1)),
                        satisfies(BYTES, val -> val.isEqualTo(Value.of(new byte[] {1, 2, 3})))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    // Missing BEAR
                    .hasAttributesSatisfyingExactly(
                        satisfies(WARM, val -> val.isTrue()),
                        satisfies(TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
                        satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                        satisfies(COLORS, val -> val.containsExactly("red", "blue")),
                        satisfies(CONDITIONS, val -> val.containsExactly(false, true)),
                        satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
                        satisfies(COINS, val -> val.containsExactly(0.01, 0.05, 0.1)),
                        satisfies(BYTES, val -> val.isEqualTo(Value.of(new byte[] {1, 2, 3})))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    // Extra CAT
                    .hasAttributesSatisfyingExactly(
                        satisfies(BEAR, val -> val.startsWith("mya")),
                        satisfies(CAT, val -> val.startsWith("nya")),
                        satisfies(WARM, val -> val.isTrue()),
                        satisfies(TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
                        satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                        satisfies(COLORS, val -> val.containsExactly("red", "blue")),
                        satisfies(CONDITIONS, val -> val.containsExactly(false, true)),
                        satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
                        satisfies(COINS, val -> val.containsExactly(0.01, 0.05, 0.1)),
                        satisfies(BYTES, val -> val.isEqualTo(Value.of(new byte[] {1, 2, 3})))))
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
                        attributes -> assertThat(attributes).containsKey(stringKey("cat"))))
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
                    .hasAttributesSatisfying(
                        attributes -> assertThat(attributes).doesNotContainKey(stringKey("bear"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfying(
                        attributes -> assertThat(attributes).doesNotContainKey("bear")))
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
                                    stringKey("bear"), value -> assertThat(value).hasSize(2))))
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
            () ->
                assertThat(SPAN1)
                    .hasEventsSatisfying(
                        events ->
                            assertThat(events.get(2))
                                .hasAttributesSatisfyingExactly(
                                    equalTo(
                                        stringKey("exception.type"),
                                        "java.lang.IllegalArgumentException"),
                                    equalTo(stringKey("exception.message"), "bad argument"))))
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
    assertThatThrownBy(() -> assertThat(SPAN1).hasStatusSatisfying(StatusDataAssert::isError))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(SPAN1).hasStatusSatisfying(status -> status.hasCode(StatusCode.ERROR)))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasStatusSatisfying(status -> status.hasDescriptionMatching("test")))
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
                    .containsOnly(entry(stringKey("dog"), "bark"), entry(stringKey("cat"), "meow")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(RESOURCE.getAttributes()).containsOnly(entry(stringKey("cat"), "meow")))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  @SuppressWarnings("Convert2MethodRef")
  void optionalAttributes() {
    assertThat(SPAN1)
        .hasAttributesSatisfyingExactly(
            // Not null
            satisfies(
                BEAR,
                val ->
                    val.satisfiesAnyOf(
                        v -> assertThat(v).isNull(), v -> assertThat(v).isEqualTo("mya"))),
            // Yes null
            satisfies(
                UNSET,
                val ->
                    val.satisfiesAnyOf(
                        v -> assertThat(v).isNull(), v -> assertThat(v).isEqualTo("objection"))),
            satisfies(WARM, val -> val.isTrue()),
            satisfies(TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
            satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
            satisfies(COLORS, val -> val.containsExactly("red", "blue")),
            satisfies(CONDITIONS, val -> val.containsExactly(false, true)),
            satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
            satisfies(COINS, val -> val.containsExactly(0.01, 0.05, 0.1)),
            satisfies(
                BYTES,
                val ->
                    val.extracting(v -> ((ByteBuffer) v.getValue()).get(0)).isEqualTo((byte) 1)));

    assertThatThrownBy(
            () ->
                assertThat(SPAN1)
                    .hasAttributesSatisfyingExactly(
                        // Yes null
                        satisfies(
                            UNSET,
                            val ->
                                val.satisfiesAnyOf(
                                    v -> assertThat(v).isNull(),
                                    v -> assertThat(v).isEqualTo("objection"))),
                        satisfies(WARM, val -> val.isTrue()),
                        satisfies(TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
                        satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                        satisfies(COLORS, val -> val.containsExactly("red", "blue")),
                        satisfies(CONDITIONS, val -> val.containsExactly(false, true)),
                        satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
                        satisfies(COINS, val -> val.containsExactly(0.01, 0.05, 0.1)),
                        satisfies(
                            BYTES,
                            val ->
                                val.extracting(v -> ((ByteBuffer) v.getValue()).get(0))
                                    .isEqualTo((byte) 1))))
        .isInstanceOf(AssertionError.class);
  }

  private static TestSpanData buildTestSpan(String spanId, String spanName) {
    return TestSpanData.builder()
        .setResource(RESOURCE)
        .setInstrumentationScopeInfo(INSTRUMENTATION_SCOPE_INFO)
        .setName(spanName)
        .setKind(SpanKind.CLIENT)
        .setStartEpochNanos(100)
        .setStatus(StatusData.ok())
        .setEndEpochNanos(200)
        .setHasEnded(true)
        .setSpanContext(SpanContext.create(TRACE_ID, spanId, TraceFlags.getSampled(), TRACE_STATE))
        .build();
  }

  @Test
  void hasSpansSatisfyingExactly() {
    Collection<List<SpanData>> traces = new ArrayList<>();
    traces.add(Arrays.asList(buildTestSpan(SPAN_ID1, "span1"), buildTestSpan(SPAN_ID2, "span2")));

    // test asserting spans in exact oder
    TracesAssert.assertThat(traces)
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactly(
                    span -> span.hasSpanId(SPAN_ID1), span -> span.hasSpanId(SPAN_ID2)));
    // wrong number of spans
    assertThatThrownBy(
            () ->
                TracesAssert.assertThat(traces)
                    .hasTracesSatisfyingExactly(
                        trace -> trace.hasSpansSatisfyingExactly(span -> span.hasSpanId(SPAN_ID1))))
        .isInstanceOf(AssertionError.class)
        .hasMessageStartingWith(
            "[Trace 0] " + System.lineSeparator() + "Expected size: 1 but was: 2");

    // test asserting spans in wrong oder
    assertThatThrownBy(
            () ->
                TracesAssert.assertThat(traces)
                    .hasTracesSatisfyingExactly(
                        trace ->
                            trace.hasSpansSatisfyingExactly(
                                span -> span.hasSpanId(SPAN_ID2),
                                span -> span.hasSpanId(SPAN_ID1))))
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "[Span 0] Expected span [span1] to have span ID <0000000000000004> but was <0000000000000003>");

    // test asserting spans in any order
    TracesAssert.assertThat(traces)
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactlyInAnyOrder(
                    span -> span.hasSpanId(SPAN_ID1), span -> span.hasSpanId(SPAN_ID2)));
    TracesAssert.assertThat(traces)
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactlyInAnyOrder(
                    span -> span.hasSpanId(SPAN_ID2), span -> span.hasSpanId(SPAN_ID1)));
    // test not asserting all spans
    assertThatThrownBy(
            () ->
                TracesAssert.assertThat(traces)
                    .hasTracesSatisfyingExactly(
                        trace ->
                            trace.hasSpansSatisfyingExactlyInAnyOrder(
                                span -> span.hasSpanId(SPAN_ID1),
                                span -> span.hasSpanId(SPAN_ID1))))
        .isInstanceOf(AssertionError.class);
  }
}
