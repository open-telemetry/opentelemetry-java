/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class RecordEventsReadableSpanTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final String SPAN_NEW_NAME = "NewName";
  private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
  private static final long START_EPOCH_NANOS = 1000_123_789_654L;

  private final IdGenerator idsGenerator = IdGenerator.random();
  private final String traceId = idsGenerator.generateTraceId();
  private final String spanId = idsGenerator.generateSpanId();
  private final String parentSpanId = idsGenerator.generateSpanId();
  private final SpanContext spanContext =
      SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
  private final Resource resource = Resource.empty();
  private final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create("theName", null);
  private final Map<AttributeKey, Object> attributes = new HashMap<>();
  private Attributes expectedAttributes;
  private final LinkData link = LinkData.create(spanContext);
  @Mock private SpanProcessor spanProcessor;

  private TestClock testClock;

  @BeforeEach
  void setUp() {
    attributes.put(stringKey("MyStringAttributeKey"), "MyStringAttributeValue");
    attributes.put(longKey("MyLongAttributeKey"), 123L);
    attributes.put(booleanKey("MyBooleanAttributeKey"), false);
    AttributesBuilder builder =
        Attributes.builder().put("MySingleStringAttributeKey", "MySingleStringAttributeValue");
    for (Map.Entry<AttributeKey, Object> entry : attributes.entrySet()) {
      builder.put(entry.getKey(), entry.getValue());
    }
    expectedAttributes = builder.build();
    testClock = TestClock.create(Instant.ofEpochSecond(0, START_EPOCH_NANOS));
  }

  @Test
  void nothingChangedAfterEnd() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    span.end();
    // Check that adding trace events or update fields after Span#end() does not throw any thrown
    // and are ignored.
    spanDoWork(span, StatusCode.ERROR, "CANCELLED");
    SpanData spanData = span.toSpanData();
    verifySpanData(
        spanData,
        Attributes.empty(),
        Collections.emptyList(),
        Collections.singletonList(link),
        SPAN_NAME,
        START_EPOCH_NANOS,
        START_EPOCH_NANOS,
        StatusData.unset(),
        /*hasEnded=*/ true);
  }

  @Test
  void endSpanTwice_DoNotCrash() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    assertThat(span.hasEnded()).isFalse();
    span.end();
    assertThat(span.hasEnded()).isTrue();
    span.end();
    assertThat(span.hasEnded()).isTrue();
  }

  @Test
  void toSpanData_ActiveSpan() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      assertThat(span.hasEnded()).isFalse();
      spanDoWork(span, null, null);
      SpanData spanData = span.toSpanData();
      EventData event =
          EventData.create(START_EPOCH_NANOS + NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
      verifySpanData(
          spanData,
          expectedAttributes,
          Collections.singletonList(event),
          Collections.singletonList(link),
          SPAN_NEW_NAME,
          START_EPOCH_NANOS,
          0,
          StatusData.unset(),
          /*hasEnded=*/ false);
      assertThat(span.hasEnded()).isFalse();
      assertThat(span.isRecording()).isTrue();
    } finally {
      span.end();
    }
    assertThat(span.hasEnded()).isTrue();
    assertThat(span.isRecording()).isFalse();
  }

  @Test
  void toSpanData_EndedSpan() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      spanDoWork(span, StatusCode.ERROR, "CANCELLED");
    } finally {
      span.end();
    }
    Mockito.verify(spanProcessor, Mockito.times(1)).onEnd(span);
    SpanData spanData = span.toSpanData();
    EventData event =
        EventData.create(START_EPOCH_NANOS + NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
    verifySpanData(
        spanData,
        expectedAttributes,
        Collections.singletonList(event),
        Collections.singletonList(link),
        SPAN_NEW_NAME,
        START_EPOCH_NANOS,
        testClock.now(),
        StatusData.create(StatusCode.ERROR, "CANCELLED"),
        /*hasEnded=*/ true);
  }

  @Test
  void toSpanData_immutableLinks() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    SpanData spanData = span.toSpanData();

    assertThatThrownBy(() -> spanData.getLinks().add(LinkData.create(SpanContext.getInvalid())))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toSpanData_immutableEvents() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    SpanData spanData = span.toSpanData();

    assertThatThrownBy(
            () -> spanData.getEvents().add(EventData.create(1000, "test", Attributes.empty())))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toSpanData_immutableEvents_ended() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    span.end();
    SpanData spanData = span.toSpanData();

    assertThatThrownBy(
            () -> spanData.getEvents().add(EventData.create(1000, "test", Attributes.empty())))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toSpanData_RootSpan() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      spanDoWork(span, null, null);
    } finally {
      span.end();
    }
    assertThat(span.getParentSpanContext().isValid()).isFalse();
    SpanData spanData = span.toSpanData();
    assertThat(SpanId.isValid(spanData.getParentSpanId())).isFalse();
  }

  @Test
  void toSpanData_ChildSpan() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      spanDoWork(span, null, null);
    } finally {
      span.end();
    }
    assertThat(span.getParentSpanContext().isValid()).isTrue();
    assertThat(span.getParentSpanContext().getTraceId()).isEqualTo(traceId);
    assertThat(span.getParentSpanContext().getSpanId()).isEqualTo(parentSpanId);
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
  }

  @Test
  void toSpanData_WithInitialAttributes() {
    RecordEventsReadableSpan span = createTestSpanWithAttributes(attributes);
    span.setAttribute("anotherKey", "anotherValue");
    span.end();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(attributes.size() + 1);
    assertThat(spanData.getTotalAttributeCount()).isEqualTo(attributes.size() + 1);
  }

  @Test
  void toSpanData_SpanDataDoesNotChangeWhenModifyingSpan() {
    // Create a span
    RecordEventsReadableSpan span = createTestSpanWithAttributes(attributes);

    // Convert it to a SpanData object -- this should be an immutable snapshot.
    SpanData spanData = span.toSpanData();

    // Now modify the span after creating the snapshot.
    span.setAttribute("anotherKey", "anotherValue");
    span.updateName("changedName");
    span.addEvent("newEvent");
    span.end();

    // Assert that the snapshot does not reflect the modified state, but the state of the time when
    // toSpanData was called.
    assertThat(spanData.getAttributes().size()).isEqualTo(attributes.size());
    assertThat(spanData.getAttributes().get(stringKey("anotherKey"))).isNull();
    assertThat(spanData.hasEnded()).isFalse();
    assertThat(spanData.getEndEpochNanos()).isEqualTo(0);
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getEvents()).isEmpty();

    // Sanity check: Calling toSpanData again after modifying the span should get us the modified
    // state.
    spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(attributes.size() + 1);
    assertThat(spanData.getAttributes().get(stringKey("anotherKey"))).isEqualTo("anotherValue");
    assertThat(spanData.hasEnded()).isTrue();
    assertThat(spanData.getEndEpochNanos()).isGreaterThan(0);
    assertThat(spanData.getName()).isEqualTo("changedName");
    assertThat(spanData.getEvents()).hasSize(1);
  }

  @Test
  void toSpanData_Status() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.CONSUMER);
    try {
      testClock.advance(Duration.ofSeconds(1));
      assertThat(span.toSpanData().getStatus()).isEqualTo(StatusData.unset());
      span.setStatus(StatusCode.ERROR, "CANCELLED");
      assertThat(span.toSpanData().getStatus())
          .isEqualTo(StatusData.create(StatusCode.ERROR, "CANCELLED"));
    } finally {
      span.end();
    }
    assertThat(span.toSpanData().getStatus())
        .isEqualTo(StatusData.create(StatusCode.ERROR, "CANCELLED"));
  }

  @Test
  void toSpanData_Kind() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.SERVER);
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(SpanKind.SERVER);
    } finally {
      span.end();
    }
  }

  @Test
  void getKind() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.SERVER);
    try {
      assertThat(span.getKind()).isEqualTo(SpanKind.SERVER);
    } finally {
      span.end();
    }
  }

  @Test
  void getAttribute() {
    RecordEventsReadableSpan span = createTestSpanWithAttributes(attributes);
    try {
      assertThat(span.getAttribute(longKey("MyLongAttributeKey"))).isEqualTo(123L);
    } finally {
      span.end();
    }
  }

  @Test
  void getInstrumentationLibraryInfo() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.CLIENT);
    try {
      assertThat(span.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
    } finally {
      span.end();
    }
  }

  @Test
  void getAndUpdateSpanName() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      assertThat(span.getName()).isEqualTo(SPAN_NAME);
      span.updateName(SPAN_NEW_NAME);
      assertThat(span.getName()).isEqualTo(SPAN_NEW_NAME);
    } finally {
      span.end();
    }
  }

  @Test
  void getLatencyNs_ActiveSpan() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    try {
      testClock.advance(Duration.ofSeconds(1));
      long elapsedTimeNanos1 = testClock.now() - START_EPOCH_NANOS;
      assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos1);
      testClock.advance(Duration.ofSeconds(1));
      long elapsedTimeNanos2 = testClock.now() - START_EPOCH_NANOS;
      assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos2);
    } finally {
      span.end();
    }
  }

  @Test
  void getLatencyNs_EndedSpan() {
    RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    testClock.advance(Duration.ofSeconds(1));
    span.end();
    long elapsedTimeNanos = testClock.now() - START_EPOCH_NANOS;
    assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos);
    testClock.advance(Duration.ofSeconds(1));
    assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos);
  }

  @Test
  void setAttribute() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      span.setAttribute("StringKey", "StringVal");
      span.setAttribute("NullStringKey", null);
      span.setAttribute("EmptyStringKey", "");
      span.setAttribute(stringKey("NullStringAttributeValue"), null);
      span.setAttribute(stringKey("EmptyStringAttributeValue"), "");
      span.setAttribute("LongKey", 1000L);
      span.setAttribute(longKey("LongKey2"), 5);
      span.setAttribute(longKey("LongKey3"), 6L);
      span.setAttribute("DoubleKey", 10.0);
      span.setAttribute("BooleanKey", false);
      span.setAttribute(
          stringArrayKey("ArrayStringKey"), Arrays.asList("StringVal", null, "", "StringVal2"));
      span.setAttribute(longArrayKey("ArrayLongKey"), Arrays.asList(1L, 2L, 3L, 4L, 5L));
      span.setAttribute(doubleArrayKey("ArrayDoubleKey"), Arrays.asList(0.1, 2.3, 4.5, 6.7, 8.9));
      span.setAttribute(
          booleanArrayKey("ArrayBooleanKey"), Arrays.asList(true, false, false, true));
      // These should be dropped
      span.setAttribute(stringArrayKey("NullArrayStringKey"), null);
      span.setAttribute(longArrayKey("NullArrayLongKey"), null);
      span.setAttribute(doubleArrayKey("NullArrayDoubleKey"), null);
      span.setAttribute(booleanArrayKey("NullArrayBooleanKey"), null);
      // These should be maintained
      span.setAttribute(longArrayKey("ArrayWithNullLongKey"), Arrays.asList(new Long[] {null}));
      span.setAttribute(
          stringArrayKey("ArrayWithNullStringKey"), Arrays.asList(new String[] {null}));
      span.setAttribute(
          doubleArrayKey("ArrayWithNullDoubleKey"), Arrays.asList(new Double[] {null}));
      span.setAttribute(
          booleanArrayKey("ArrayWithNullBooleanKey"), Arrays.asList(new Boolean[] {null}));
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(16);
    assertThat(spanData.getAttributes().get(stringKey("StringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringKey("EmptyStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringKey("EmptyStringAttributeValue"))).isNotNull();
    assertThat(spanData.getAttributes().get(longKey("LongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longKey("LongKey2"))).isEqualTo(5L);
    assertThat(spanData.getAttributes().get(longKey("LongKey3"))).isEqualTo(6L);
    assertThat(spanData.getAttributes().get(doubleKey("DoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanKey("BooleanKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayLongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayDoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayBooleanKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayWithNullLongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayWithNullStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayWithNullDoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayWithNullBooleanKey")))
        .isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayStringKey")).size()).isEqualTo(4);
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayLongKey")).size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayDoubleKey")).size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayBooleanKey")).size())
        .isEqualTo(4);
  }

  @Test
  void setAttribute_emptyKeys() {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAttribute("", "");
    span.setAttribute("", 1000L);
    span.setAttribute("", 10.0);
    span.setAttribute("", false);
    span.setAttribute(stringArrayKey(""), Collections.emptyList());
    span.setAttribute(booleanArrayKey(""), Collections.emptyList());
    span.setAttribute(longArrayKey(""), Collections.emptyList());
    span.setAttribute(doubleArrayKey(""), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(0);
  }

  @Test
  void setAttribute_nullKeys() {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAttribute(stringKey(null), "");
    span.setAttribute(null, 1000L);
    span.setAttribute(null, 10.0);
    span.setAttribute(null, false);
    span.setAttribute(null, Collections.emptyList());
    span.setAttribute(null, Collections.emptyList());
    span.setAttribute(null, Collections.emptyList());
    span.setAttribute(null, Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isZero();
  }

  @Test
  void setAttribute_emptyArrayAttributeValue() {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAttribute(stringArrayKey("stringArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isZero();
    span.setAttribute(stringArrayKey("stringArrayAttribute"), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(1);
    span.setAttribute(booleanArrayKey("boolArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(1);
    span.setAttribute(booleanArrayKey("boolArrayAttribute"), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
    span.setAttribute(longArrayKey("longArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
    span.setAttribute(longArrayKey("longArrayAttribute"), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(3);
    span.setAttribute(doubleArrayKey("doubleArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(3);
    span.setAttribute(doubleArrayKey("doubleArrayAttribute"), Collections.emptyList());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(4);
  }

  @Test
  void setAttribute_nullStringValue() {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAttribute("nullString", null);
    span.setAttribute("emptyString", "");
    span.setAttribute(stringKey("nullStringAttributeValue"), null);
    span.setAttribute(stringKey("emptyStringAttributeValue"), "");
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
  }

  @Test
  void setAttribute_nullAttributeValue() {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAttribute("emptyString", "");
    span.setAttribute(stringKey("nullString"), null);
    span.setAttribute(stringKey("nullStringAttributeValue"), null);
    span.setAttribute(stringKey("emptyStringAttributeValue"), "");
    span.setAttribute("longAttribute", 0L);
    span.setAttribute("boolAttribute", false);
    span.setAttribute("doubleAttribute", 0.12345f);
    span.setAttribute(stringArrayKey("stringArrayAttribute"), Arrays.asList("", null));
    span.setAttribute(booleanArrayKey("boolArrayAttribute"), Arrays.asList(true, null));
    span.setAttribute(longArrayKey("longArrayAttribute"), Arrays.asList(12345L, null));
    span.setAttribute(doubleArrayKey("doubleArrayAttribute"), Arrays.asList(1.2345, null));
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(9);
  }

  @Test
  void setAllAttributes() {
    RecordEventsReadableSpan span = createTestRootSpan();
    Attributes attributes =
        Attributes.builder()
            .put("StringKey", "StringVal")
            .put("NullStringKey", (String) null)
            .put("EmptyStringKey", "")
            .put(stringKey("NullStringAttributeValue"), null)
            .put(stringKey("EmptyStringAttributeValue"), "")
            .put("LongKey", 1000L)
            .put(longKey("LongKey2"), 5)
            .put(longKey("LongKey3"), 6L)
            .put("DoubleKey", 10.0)
            .put("BooleanKey", false)
            .put(
                stringArrayKey("ArrayStringKey"),
                Arrays.asList("StringVal", null, "", "StringVal2"))
            .put(longArrayKey("ArrayLongKey"), Arrays.asList(1L, 2L, 3L, 4L, 5L))
            .put(doubleArrayKey("ArrayDoubleKey"), Arrays.asList(0.1, 2.3, 4.5, 6.7, 8.9))
            .put(booleanArrayKey("ArrayBooleanKey"), Arrays.asList(true, false, false, true))
            // These should be dropped
            .put(stringArrayKey("NullArrayStringKey"), null)
            .put(longArrayKey("NullArrayLongKey"), null)
            .put(doubleArrayKey("NullArrayDoubleKey"), null)
            .put(booleanArrayKey("NullArrayBooleanKey"), null)
            // These should be maintained
            .put(longArrayKey("ArrayWithNullLongKey"), Arrays.asList(new Long[] {null}))
            .put(stringArrayKey("ArrayWithNullStringKey"), Arrays.asList(new String[] {null}))
            .put(doubleArrayKey("ArrayWithNullDoubleKey"), Arrays.asList(new Double[] {null}))
            .put(booleanArrayKey("ArrayWithNullBooleanKey"), Arrays.asList(new Boolean[] {null}))
            .build();

    try {
      span.setAllAttributes(attributes);
    } finally {
      span.end();
    }

    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(16);
    assertThat(spanData.getAttributes().get(stringKey("StringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringKey("EmptyStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringKey("EmptyStringAttributeValue"))).isNotNull();
    assertThat(spanData.getAttributes().get(longKey("LongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longKey("LongKey2"))).isEqualTo(5L);
    assertThat(spanData.getAttributes().get(longKey("LongKey3"))).isEqualTo(6L);
    assertThat(spanData.getAttributes().get(doubleKey("DoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanKey("BooleanKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayLongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayDoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayBooleanKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayWithNullLongKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayWithNullStringKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayWithNullDoubleKey"))).isNotNull();
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayWithNullBooleanKey")))
        .isNotNull();
    assertThat(spanData.getAttributes().get(stringArrayKey("ArrayStringKey")).size()).isEqualTo(4);
    assertThat(spanData.getAttributes().get(longArrayKey("ArrayLongKey")).size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(doubleArrayKey("ArrayDoubleKey")).size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(booleanArrayKey("ArrayBooleanKey")).size())
        .isEqualTo(4);
  }

  @Test
  void setAllAttributes_mergesAttributes() {
    RecordEventsReadableSpan span = createTestRootSpan();
    Attributes attributes =
        Attributes.builder()
            .put("StringKey", "StringVal")
            .put("LongKey", 1000L)
            .put("DoubleKey", 10.0)
            .put("BooleanKey", false)
            .build();

    try {
      span.setAttribute("StringKey", "OtherStringVal")
          .setAttribute("ExistingStringKey", "ExistingStringVal")
          .setAttribute("LongKey", 2000L)
          .setAllAttributes(attributes);
    } finally {
      span.end();
    }

    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(5);
    assertThat(spanData.getAttributes().get(stringKey("StringKey")))
        .isNotNull()
        .isEqualTo("StringVal");
    assertThat(spanData.getAttributes().get(stringKey("ExistingStringKey")))
        .isNotNull()
        .isEqualTo("ExistingStringVal");
    assertThat(spanData.getAttributes().get(longKey("LongKey"))).isNotNull().isEqualTo(1000L);
    assertThat(spanData.getAttributes().get(doubleKey("DoubleKey"))).isNotNull().isEqualTo(10.0);
    assertThat(spanData.getAttributes().get(booleanKey("BooleanKey"))).isNotNull().isEqualTo(false);
  }

  @Test
  void setAllAttributes_nullAttributes() {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAllAttributes(null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(0);
  }

  @Test
  void setAllAttributes_emptyAttributes() {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAllAttributes(Attributes.empty());
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(0);
  }

  @Test
  void addEvent() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      span.addEvent("event1");
      span.addEvent("event2", Attributes.of(stringKey("e1key"), "e1Value"));
      span.addEvent("event3", 10, TimeUnit.SECONDS);
      span.addEvent("event4", Instant.ofEpochSecond(20));
      span.addEvent(
          "event5", Attributes.builder().put("foo", "bar").build(), 30, TimeUnit.MILLISECONDS);
      span.addEvent(
          "event6", Attributes.builder().put("foo", "bar").build(), Instant.ofEpochMilli(1000));
    } finally {
      span.end();
    }
    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(6);
    assertThat(events.get(0))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event1");
              assertThat(event.getAttributes()).isEqualTo(Attributes.empty());
              assertThat(event.getEpochNanos()).isEqualTo(START_EPOCH_NANOS);
            });
    assertThat(events.get(1))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event2");
              assertThat(event.getAttributes())
                  .isEqualTo(Attributes.of(stringKey("e1key"), "e1Value"));
              assertThat(event.getEpochNanos()).isEqualTo(START_EPOCH_NANOS);
            });
    assertThat(events.get(2))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event3");
              assertThat(event.getAttributes()).isEqualTo(Attributes.empty());
              assertThat(event.getEpochNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(10));
            });
    assertThat(events.get(3))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event4");
              assertThat(event.getAttributes()).isEqualTo(Attributes.empty());
              assertThat(event.getEpochNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(20));
            });
    assertThat(events.get(4))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event5");
              assertThat(event.getAttributes())
                  .isEqualTo(Attributes.builder().put("foo", "bar").build());
              assertThat(event.getEpochNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(30));
            });
    assertThat(events.get(5))
        .satisfies(
            event -> {
              assertThat(event.getName()).isEqualTo("event6");
              assertThat(event.getAttributes())
                  .isEqualTo(Attributes.builder().put("foo", "bar").build());
              assertThat(event.getEpochNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(1000));
            });
  }

  @Test
  void attributeLength() {
    int maxLength = 25;
    RecordEventsReadableSpan span =
        createTestSpan(SpanLimits.builder().setMaxAttributeLength(maxLength).build());
    try {
      String strVal = IntStream.range(0, maxLength).mapToObj(i -> "a").collect(joining());
      String tooLongStrVal = strVal + strVal;

      Attributes attributes =
          Attributes.builder()
              .put("string", tooLongStrVal)
              .put("boolean", true)
              .put("long", 1L)
              .put("double", 1.0)
              .put(stringArrayKey("stringArray"), Arrays.asList(strVal, tooLongStrVal))
              .put(booleanArrayKey("booleanArray"), Arrays.asList(true, false))
              .put(longArrayKey("longArray"), Arrays.asList(1L, 2L))
              .put(doubleArrayKey("doubleArray"), Arrays.asList(1.0, 2.0))
              .build();
      span.setAllAttributes(attributes);

      attributes = span.toSpanData().getAttributes();
      assertThat(attributes.get(stringKey("string"))).isEqualTo(strVal);
      assertThat(attributes.get(booleanKey("boolean"))).isEqualTo(true);
      assertThat(attributes.get(longKey("long"))).isEqualTo(1L);
      assertThat(attributes.get(doubleKey("double"))).isEqualTo(1.0);
      assertThat(attributes.get(stringArrayKey("stringArray")))
          .isEqualTo(Arrays.asList(strVal, strVal));
      assertThat(attributes.get(booleanArrayKey("booleanArray")))
          .isEqualTo(Arrays.asList(true, false));
      assertThat(attributes.get(longArrayKey("longArray"))).isEqualTo(Arrays.asList(1L, 2L));
      assertThat(attributes.get(doubleArrayKey("doubleArray"))).isEqualTo(Arrays.asList(1.0, 2.0));
    } finally {
      span.end();
    }
  }

  @Test
  void eventAttributeLength() {
    int maxLength = 25;
    RecordEventsReadableSpan span =
        createTestSpan(SpanLimits.builder().setMaxAttributeLength(maxLength).build());
    try {
      String strVal = IntStream.range(0, maxLength).mapToObj(i -> "a").collect(joining());
      String tooLongStrVal = strVal + strVal;

      Attributes attributes =
          Attributes.builder()
              .put("string", tooLongStrVal)
              .put("boolean", true)
              .put("long", 1L)
              .put("double", 1.0)
              .put(stringArrayKey("stringArray"), Arrays.asList(strVal, tooLongStrVal))
              .put(booleanArrayKey("booleanArray"), Arrays.asList(true, false))
              .put(longArrayKey("longArray"), Arrays.asList(1L, 2L))
              .put(doubleArrayKey("doubleArray"), Arrays.asList(1.0, 2.0))
              .build();
      span.setAllAttributes(attributes);

      attributes = span.toSpanData().getAttributes();
      assertThat(attributes.get(stringKey("string"))).isEqualTo(strVal);
      assertThat(attributes.get(booleanKey("boolean"))).isEqualTo(true);
      assertThat(attributes.get(longKey("long"))).isEqualTo(1L);
      assertThat(attributes.get(doubleKey("double"))).isEqualTo(1.0);
      assertThat(attributes.get(stringArrayKey("stringArray")))
          .isEqualTo(Arrays.asList(strVal, strVal));
      assertThat(attributes.get(booleanArrayKey("booleanArray")))
          .isEqualTo(Arrays.asList(true, false));
      assertThat(attributes.get(longArrayKey("longArray"))).isEqualTo(Arrays.asList(1L, 2L));
      assertThat(attributes.get(doubleArrayKey("doubleArray"))).isEqualTo(Arrays.asList(1.0, 2.0));
    } finally {
      span.end();
    }
  }

  @Test
  void droppingAttributes() {
    final int maxNumberOfAttributes = 8;
    SpanLimits spanLimits =
        SpanLimits.builder().setMaxNumberOfAttributes(maxNumberOfAttributes).build();
    RecordEventsReadableSpan span = createTestSpan(spanLimits);
    try {
      for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
        span.setAttribute(longKey("MyStringAttributeKey" + i), (long) i);
      }
      SpanData spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      assertThat(spanData.getTotalAttributeCount()).isEqualTo(2 * maxNumberOfAttributes);
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
    assertThat(spanData.getTotalAttributeCount()).isEqualTo(2 * maxNumberOfAttributes);
  }

  @Test
  void endWithTimestamp_numeric() {
    RecordEventsReadableSpan span1 = createTestRootSpan();
    span1.end(10, TimeUnit.NANOSECONDS);
    assertThat(span1.toSpanData().getEndEpochNanos()).isEqualTo(10);
  }

  @Test
  void endWithTimestamp_instant() {
    RecordEventsReadableSpan span1 = createTestRootSpan();
    span1.end(Instant.ofEpochMilli(10));
    assertThat(span1.toSpanData().getEndEpochNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(10));
  }

  @Test
  void droppingAndAddingAttributes() {
    final int maxNumberOfAttributes = 8;
    SpanLimits spanLimits =
        SpanLimits.builder().setMaxNumberOfAttributes(maxNumberOfAttributes).build();
    RecordEventsReadableSpan span = createTestSpan(spanLimits);
    try {
      for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
        span.setAttribute(longKey("MyStringAttributeKey" + i), (long) i);
      }
      SpanData spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      assertThat(spanData.getTotalAttributeCount()).isEqualTo(2 * maxNumberOfAttributes);

      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        int val = i + maxNumberOfAttributes * 3 / 2;
        span.setAttribute(longKey("MyStringAttributeKey" + i), (long) val);
      }
      spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      // Test that we still have in the attributes map the latest maxNumberOfAttributes / 2 entries.
      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        int val = i + maxNumberOfAttributes * 3 / 2;
        assertThat(spanData.getAttributes().get(longKey("MyStringAttributeKey" + i)))
            .isEqualTo(val);
      }
      // Test that we have the newest re-added initial entries.
      for (int i = maxNumberOfAttributes / 2; i < maxNumberOfAttributes; i++) {
        assertThat(spanData.getAttributes().get(longKey("MyStringAttributeKey" + i))).isEqualTo(i);
      }
    } finally {
      span.end();
    }
  }

  @Test
  void droppingEvents() {
    final int maxNumberOfEvents = 8;
    SpanLimits spanLimits = SpanLimits.builder().setMaxNumberOfEvents(maxNumberOfEvents).build();
    RecordEventsReadableSpan span = createTestSpan(spanLimits);
    try {
      for (int i = 0; i < 2 * maxNumberOfEvents; i++) {
        span.addEvent("event2", Attributes.empty());
        testClock.advance(Duration.ofSeconds(1));
      }
      SpanData spanData = span.toSpanData();

      assertThat(spanData.getEvents().size()).isEqualTo(maxNumberOfEvents);
      for (int i = 0; i < maxNumberOfEvents; i++) {
        EventData expectedEvent =
            EventData.create(
                START_EPOCH_NANOS + i * NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
        assertThat(spanData.getEvents().get(i)).isEqualTo(expectedEvent);
        assertThat(spanData.getTotalRecordedEvents()).isEqualTo(2 * maxNumberOfEvents);
      }
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getEvents().size()).isEqualTo(maxNumberOfEvents);
    for (int i = 0; i < maxNumberOfEvents; i++) {
      EventData expectedEvent =
          EventData.create(
              START_EPOCH_NANOS + i * NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
      assertThat(spanData.getEvents().get(i)).isEqualTo(expectedEvent);
    }
  }

  @Test
  void recordException() {
    IllegalStateException exception = new IllegalStateException("there was an exception");
    RecordEventsReadableSpan span = createTestRootSpan();

    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    String stacktrace = writer.toString();

    testClock.advance(Duration.ofNanos(1000));
    long timestamp = testClock.now();

    span.recordException(exception);

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    EventData event = events.get(0);
    assertThat(event.getName()).isEqualTo("exception");
    assertThat(event.getEpochNanos()).isEqualTo(timestamp);
    assertThat(event.getAttributes())
        .isEqualTo(
            Attributes.builder()
                .put(SemanticAttributes.EXCEPTION_TYPE, "java.lang.IllegalStateException")
                .put(SemanticAttributes.EXCEPTION_MESSAGE, "there was an exception")
                .put(SemanticAttributes.EXCEPTION_STACKTRACE, stacktrace)
                .build());
  }

  @Test
  void recordException_noMessage() {
    IllegalStateException exception = new IllegalStateException();
    RecordEventsReadableSpan span = createTestRootSpan();

    span.recordException(exception);

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    EventData event = events.get(0);
    assertThat(event.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE)).isNull();
  }

  private static class InnerClassException extends Exception {}

  @Test
  void recordException_innerClassException() {
    InnerClassException exception = new InnerClassException();
    RecordEventsReadableSpan span = createTestRootSpan();

    span.recordException(exception);

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    EventData event = events.get(0);
    assertThat(event.getAttributes().get(SemanticAttributes.EXCEPTION_TYPE))
        .isEqualTo("io.opentelemetry.sdk.trace.RecordEventsReadableSpanTest.InnerClassException");
  }

  @Test
  void recordException_additionalAttributes() {
    IllegalStateException exception = new IllegalStateException("there was an exception");
    RecordEventsReadableSpan span = createTestRootSpan();

    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    String stacktrace = writer.toString();

    testClock.advance(Duration.ofNanos(1000));
    long timestamp = testClock.now();

    span.recordException(
        exception,
        Attributes.of(
            stringKey("key1"),
            "this is an additional attribute",
            stringKey("exception.message"),
            "this is a precedence attribute"));

    List<EventData> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    EventData event = events.get(0);
    assertThat(event.getName()).isEqualTo("exception");
    assertThat(event.getEpochNanos()).isEqualTo(timestamp);
    assertThat(event.getAttributes())
        .isEqualTo(
            Attributes.builder()
                .put("key1", "this is an additional attribute")
                .put("exception.type", "java.lang.IllegalStateException")
                .put("exception.message", "this is a precedence attribute")
                .put("exception.stacktrace", stacktrace)
                .build());
  }

  @Test
  void badArgsIgnored() {
    RecordEventsReadableSpan span = createTestRootSpan();

    // Should be no exceptions
    span.setAttribute(null, 0L);
    span.setStatus(null);
    span.setStatus(null, null);
    span.updateName(null);
    span.addEvent(null);
    span.addEvent(null, 0, null);
    span.addEvent("event", 0, null);
    span.addEvent(null, (Attributes) null);
    span.addEvent("event", (Attributes) null);
    span.addEvent(null, (Instant) null);
    span.addEvent(null, null, 0, null);
    span.addEvent("event", null, 0, TimeUnit.MILLISECONDS);
    span.addEvent("event", Attributes.empty(), 0, null);
    span.addEvent(null, null, null);
    span.recordException(null);
    span.end(0, TimeUnit.NANOSECONDS);
    span.end(1, null);
    span.end(null);

    // Ignored the bad calls
    SpanData data = span.toSpanData();
    assertThat(data.getAttributes().isEmpty()).isTrue();
    assertThat(data.getStatus()).isEqualTo(StatusData.unset());
    assertThat(data.getName()).isEqualTo(SPAN_NAME);
  }

  private RecordEventsReadableSpan createTestSpanWithAttributes(
      Map<AttributeKey, Object> attributes) {
    SpanLimits spanLimits = SpanLimits.getDefault();
    AttributesMap attributesMap =
        new AttributesMap(
            spanLimits.getMaxNumberOfAttributes(), spanLimits.getMaxAttributeValueLength());
    attributes.forEach(attributesMap::put);
    return createTestSpan(
        SpanKind.INTERNAL,
        SpanLimits.getDefault(),
        null,
        attributesMap,
        Collections.singletonList(link));
  }

  private RecordEventsReadableSpan createTestRootSpan() {
    return createTestSpan(
        SpanKind.INTERNAL,
        SpanLimits.getDefault(),
        SpanId.getInvalid(),
        null,
        Collections.singletonList(link));
  }

  private RecordEventsReadableSpan createTestSpan(SpanKind kind) {
    return createTestSpan(
        kind, SpanLimits.getDefault(), parentSpanId, null, Collections.singletonList(link));
  }

  private RecordEventsReadableSpan createTestSpan(SpanLimits config) {
    return createTestSpan(
        SpanKind.INTERNAL, config, parentSpanId, null, Collections.singletonList(link));
  }

  private RecordEventsReadableSpan createTestSpan(
      SpanKind kind,
      SpanLimits config,
      @Nullable String parentSpanId,
      @Nullable AttributesMap attributes,
      List<LinkData> links) {

    RecordEventsReadableSpan span =
        RecordEventsReadableSpan.startSpan(
            spanContext,
            SPAN_NAME,
            instrumentationLibraryInfo,
            kind,
            parentSpanId != null
                ? Span.wrap(
                    SpanContext.create(
                        traceId, parentSpanId, TraceFlags.getDefault(), TraceState.getDefault()))
                : Span.getInvalid(),
            Context.root(),
            config,
            spanProcessor,
            testClock,
            resource,
            attributes,
            links,
            1,
            0);
    Mockito.verify(spanProcessor, Mockito.times(1)).onStart(Context.root(), span);
    return span;
  }

  private void spanDoWork(
      RecordEventsReadableSpan span,
      @Nullable StatusCode canonicalCode,
      @Nullable String descriptio) {
    span.setAttribute("MySingleStringAttributeKey", "MySingleStringAttributeValue");
    attributes.forEach(span::setAttribute);
    testClock.advance(Duration.ofSeconds(1));
    span.addEvent("event2", Attributes.empty());
    testClock.advance(Duration.ofSeconds(1));
    span.updateName(SPAN_NEW_NAME);
    if (canonicalCode != null) {
      span.setStatus(canonicalCode, descriptio);
    }
  }

  private void verifySpanData(
      SpanData spanData,
      final Attributes attributes,
      List<EventData> eventData,
      List<LinkData> links,
      String spanName,
      long startEpochNanos,
      long endEpochNanos,
      StatusData status,
      boolean hasEnded) {
    assertThat(spanData.getTraceId()).isEqualTo(traceId);
    assertThat(spanData.getSpanId()).isEqualTo(spanId);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getSpanContext().getTraceState()).isEqualTo(TraceState.getDefault());
    assertThat(spanData.getResource()).isEqualTo(resource);
    assertThat(spanData.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
    assertThat(spanData.getName()).isEqualTo(spanName);
    assertThat(spanData.getEvents()).isEqualTo(eventData);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getStartEpochNanos()).isEqualTo(startEpochNanos);
    assertThat(spanData.getEndEpochNanos()).isEqualTo(endEpochNanos);
    assertThat(spanData.getStatus().getStatusCode()).isEqualTo(status.getStatusCode());
    assertThat(spanData.hasEnded()).isEqualTo(hasEnded);

    // verify equality manually, since the implementations don't all equals with each other.
    Attributes spanDataAttributes = spanData.getAttributes();
    assertThat(spanDataAttributes.size()).isEqualTo(attributes.size());
    spanDataAttributes.forEach((key, value) -> assertThat(attributes.get(key)).isEqualTo(value));
  }

  @Test
  void testAsSpanData() {
    String name = "GreatSpan";
    SpanKind kind = SpanKind.SERVER;
    String traceId = this.traceId;
    String spanId = this.spanId;
    String parentSpanId = this.parentSpanId;
    SpanLimits spanLimits = SpanLimits.getDefault();
    SpanProcessor spanProcessor = NoopSpanProcessor.getInstance();
    TestClock clock = TestClock.create();
    Resource resource = this.resource;
    Attributes attributes = TestUtils.generateRandomAttributes();
    final AttributesMap attributesWithCapacity = new AttributesMap(32, Integer.MAX_VALUE);
    attributes.forEach((key, value) -> attributesWithCapacity.put((AttributeKey) key, value));
    Attributes event1Attributes = TestUtils.generateRandomAttributes();
    Attributes event2Attributes = TestUtils.generateRandomAttributes();
    SpanContext context =
        SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
    LinkData link1 = LinkData.create(context, TestUtils.generateRandomAttributes());

    RecordEventsReadableSpan readableSpan =
        RecordEventsReadableSpan.startSpan(
            context,
            name,
            instrumentationLibraryInfo,
            kind,
            parentSpanId != null
                ? Span.wrap(
                    SpanContext.create(
                        traceId, parentSpanId, TraceFlags.getDefault(), TraceState.getDefault()))
                : Span.getInvalid(),
            Context.root(),
            spanLimits,
            spanProcessor,
            clock,
            resource,
            attributesWithCapacity,
            Collections.singletonList(link1),
            1,
            0);
    long startEpochNanos = clock.now();
    clock.advance(Duration.ofMillis(4));
    long firstEventEpochNanos = clock.now();
    readableSpan.addEvent("event1", event1Attributes);
    clock.advance(Duration.ofMillis(6));
    long secondEventTimeNanos = clock.now();
    readableSpan.addEvent("event2", event2Attributes);

    clock.advance(Duration.ofMillis(100));
    readableSpan.end();
    long endEpochNanos = clock.now();

    List<EventData> events =
        Arrays.asList(
            EventData.create(
                firstEventEpochNanos, "event1", event1Attributes, event1Attributes.size()),
            EventData.create(
                secondEventTimeNanos, "event2", event2Attributes, event2Attributes.size()));

    SpanData result = readableSpan.toSpanData();
    verifySpanData(
        result,
        attributesWithCapacity,
        events,
        Collections.singletonList(link1),
        name,
        startEpochNanos,
        endEpochNanos,
        StatusData.unset(),
        /* hasEnded= */ true);
    assertThat(result.getTotalRecordedLinks()).isEqualTo(1);
    assertThat(result.getSpanContext().isSampled()).isEqualTo(false);
  }

  @Test
  void testConcurrentModification() throws ExecutionException, InterruptedException {
    final RecordEventsReadableSpan span = createTestSpan(SpanKind.INTERNAL);
    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<?> modifierFuture =
        es.submit(
            () -> {
              for (int i = 0; i < 5096 * 5; ++i) {
                span.setAttribute("hey" + i, "");
              }
            });
    try {
      for (int i = 0; i < 5096 * 5; ++i) {
        span.toSpanData();
      }
    } catch (Throwable t) {
      modifierFuture.cancel(true);
      throw t;
    }
    modifierFuture.get();
  }
}
