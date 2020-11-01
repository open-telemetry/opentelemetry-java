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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@SuppressWarnings({"rawtypes", "unchecked"})
class RecordEventsReadableSpanTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final String SPAN_NEW_NAME = "NewName";
  private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
  private static final long MILLIS_PER_SECOND = TimeUnit.SECONDS.toMillis(1);
  private static final boolean EXPECTED_HAS_REMOTE_PARENT = true;
  private static final long START_EPOCH_NANOS = 1000_123_789_654L;

  private final IdGenerator idsGenerator = IdGenerator.random();
  private final String traceId = idsGenerator.generateTraceId();
  private final String spanId = idsGenerator.generateSpanId();
  private final String parentSpanId = idsGenerator.generateSpanId();
  private final SpanContext spanContext =
      SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
  private final Resource resource = Resource.getEmpty();
  private final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create("theName", null);
  private final Map<AttributeKey, Object> attributes = new HashMap<>();
  private Attributes expectedAttributes;
  private final Link link = Link.create(spanContext);
  @Mock private SpanProcessor spanProcessor;

  private TestClock testClock;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    attributes.put(stringKey("MyStringAttributeKey"), "MyStringAttributeValue");
    attributes.put(longKey("MyLongAttributeKey"), 123L);
    attributes.put(booleanKey("MyBooleanAttributeKey"), false);
    AttributesBuilder builder =
        Attributes.builder().put("MySingleStringAttributeKey", "MySingleStringAttributeValue");
    for (Map.Entry<AttributeKey, Object> entry : attributes.entrySet()) {
      builder.put(entry.getKey(), entry.getValue());
    }
    expectedAttributes = builder.build();
    testClock = TestClock.create(START_EPOCH_NANOS);
  }

  @Test
  void nothingChangedAfterEnd() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
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
        Status.unset(),
        /*hasEnded=*/ true);
  }

  @Test
  void endSpanTwice_DoNotCrash() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    assertThat(span.hasEnded()).isFalse();
    span.end();
    assertThat(span.hasEnded()).isTrue();
    span.end();
    assertThat(span.hasEnded()).isTrue();
  }

  @Test
  void toSpanData_ActiveSpan() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      assertThat(span.hasEnded()).isFalse();
      spanDoWork(span, null, null);
      SpanData spanData = span.toSpanData();
      Event event =
          Event.create(START_EPOCH_NANOS + NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
      verifySpanData(
          spanData,
          expectedAttributes,
          Collections.singletonList(event),
          Collections.singletonList(link),
          SPAN_NEW_NAME,
          START_EPOCH_NANOS,
          0,
          Status.unset(),
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
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      spanDoWork(span, StatusCode.ERROR, "CANCELLED");
    } finally {
      span.end();
    }
    Mockito.verify(spanProcessor, Mockito.times(1)).onEnd(span);
    SpanData spanData = span.toSpanData();
    Event event =
        Event.create(START_EPOCH_NANOS + NANOS_PER_SECOND, "event2", Attributes.empty(), 0);
    verifySpanData(
        spanData,
        expectedAttributes,
        Collections.singletonList(event),
        Collections.singletonList(link),
        SPAN_NEW_NAME,
        START_EPOCH_NANOS,
        testClock.now(),
        Status.create(StatusCode.ERROR, "CANCELLED"),
        /*hasEnded=*/ true);
  }

  @Test
  void toSpanData_immutableLinks() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    SpanData spanData = span.toSpanData();

    assertThrows(
        UnsupportedOperationException.class,
        () -> spanData.getLinks().add(Link.create(SpanContext.getInvalid())));
  }

  @Test
  void toSpanData_immutableEvents() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    SpanData spanData = span.toSpanData();

    assertThrows(
        UnsupportedOperationException.class,
        () -> spanData.getEvents().add(Event.create(1000, "test", Attributes.empty())));
  }

  @Test
  void toSpanData_RootSpan() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      spanDoWork(span, null, null);
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(SpanId.isValid(spanData.getParentSpanId())).isFalse();
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
  void setStatus() {
    RecordEventsReadableSpan span = createTestSpan(Kind.CONSUMER);
    try {
      testClock.advanceMillis(MILLIS_PER_SECOND);
      assertThat(span.toSpanData().getStatus()).isEqualTo(Status.unset());
      span.setStatus(StatusCode.ERROR, "CANCELLED");
      assertThat(span.toSpanData().getStatus())
          .isEqualTo(Status.create(StatusCode.ERROR, "CANCELLED"));
    } finally {
      span.end();
    }
    assertThat(span.toSpanData().getStatus())
        .isEqualTo(Status.create(StatusCode.ERROR, "CANCELLED"));
  }

  @Test
  void getSpanKind() {
    RecordEventsReadableSpan span = createTestSpan(Kind.SERVER);
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(Kind.SERVER);
    } finally {
      span.end();
    }
  }

  @Test
  void getInstrumentationLibraryInfo() {
    RecordEventsReadableSpan span = createTestSpan(Kind.CLIENT);
    try {
      assertThat(span.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
    } finally {
      span.end();
    }
  }

  @Test
  void getSpanHasRemoteParent() {
    RecordEventsReadableSpan span = createTestSpan(Kind.SERVER);
    try {
      assertThat(span.toSpanData().hasRemoteParent()).isTrue();
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
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      testClock.advanceMillis(MILLIS_PER_SECOND);
      long elapsedTimeNanos1 = testClock.now() - START_EPOCH_NANOS;
      assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos1);
      testClock.advanceMillis(MILLIS_PER_SECOND);
      long elapsedTimeNanos2 = testClock.now() - START_EPOCH_NANOS;
      assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos2);
    } finally {
      span.end();
    }
  }

  @Test
  void getLatencyNs_EndedSpan() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    testClock.advanceMillis(MILLIS_PER_SECOND);
    span.end();
    long elapsedTimeNanos = testClock.now() - START_EPOCH_NANOS;
    assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos);
    testClock.advanceMillis(MILLIS_PER_SECOND);
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
  void addEvent() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      span.addEvent("event1");
      span.addEvent("event2", Attributes.of(stringKey("e1key"), "e1Value"));
    } finally {
      span.end();
    }
    List<Event> events = span.toSpanData().getEvents();
    assertThat(events.size()).isEqualTo(2);
  }

  @Test
  void droppingAttributes() {
    final int maxNumberOfAttributes = 8;
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder()
            .setMaxNumberOfAttributes(maxNumberOfAttributes)
            .build();
    RecordEventsReadableSpan span = createTestSpan(traceConfig);
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
  void droppingAndAddingAttributes() {
    final int maxNumberOfAttributes = 8;
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder()
            .setMaxNumberOfAttributes(maxNumberOfAttributes)
            .build();
    RecordEventsReadableSpan span = createTestSpan(traceConfig);
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
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder().setMaxNumberOfEvents(maxNumberOfEvents).build();
    RecordEventsReadableSpan span = createTestSpan(traceConfig);
    try {
      for (int i = 0; i < 2 * maxNumberOfEvents; i++) {
        span.addEvent("event2", Attributes.empty());
        testClock.advanceMillis(MILLIS_PER_SECOND);
      }
      SpanData spanData = span.toSpanData();

      assertThat(spanData.getEvents().size()).isEqualTo(maxNumberOfEvents);
      for (int i = 0; i < maxNumberOfEvents; i++) {
        Event expectedEvent =
            Event.create(
                START_EPOCH_NANOS + (maxNumberOfEvents + i) * NANOS_PER_SECOND,
                "event2",
                Attributes.empty(),
                0);
        assertThat(spanData.getEvents().get(i)).isEqualTo(expectedEvent);
        assertThat(spanData.getTotalRecordedEvents()).isEqualTo(2 * maxNumberOfEvents);
      }
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getEvents().size()).isEqualTo(maxNumberOfEvents);
    for (int i = 0; i < maxNumberOfEvents; i++) {
      Event expectedEvent =
          Event.create(
              START_EPOCH_NANOS + (maxNumberOfEvents + i) * NANOS_PER_SECOND,
              "event2",
              Attributes.empty(),
              0);
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

    testClock.advanceNanos(1000);
    long timestamp = testClock.now();

    span.recordException(exception);

    List<Event> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    Event event = events.get(0);
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

    List<Event> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    Event event = events.get(0);
    assertThat(event.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE)).isNull();
  }

  private static class InnerClassException extends Exception {}

  @Test
  void recordException_innerClassException() {
    InnerClassException exception = new InnerClassException();
    RecordEventsReadableSpan span = createTestRootSpan();

    span.recordException(exception);

    List<Event> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    Event event = events.get(0);
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

    testClock.advanceNanos(1000);
    long timestamp = testClock.now();

    span.recordException(
        exception,
        Attributes.of(
            stringKey("key1"),
            "this is an additional attribute",
            stringKey("exception.message"),
            "this is a precedence attribute"));

    List<Event> events = span.toSpanData().getEvents();
    assertThat(events).hasSize(1);
    Event event = events.get(0);
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
    span.addEvent(null, 0);
    span.addEvent(null, null);
    span.addEvent(null, null, 0);
    span.recordException(null);
    span.end(null);

    // Ignored the bad calls
    SpanData data = span.toSpanData();
    assertThat(data.getAttributes().isEmpty()).isTrue();
    assertThat(data.getStatus()).isEqualTo(Status.unset());
    assertThat(data.getName()).isEqualTo(SPAN_NAME);
  }

  private RecordEventsReadableSpan createTestSpanWithAttributes(
      Map<AttributeKey, Object> attributes) {
    AttributesMap attributesMap =
        new AttributesMap(TraceConfig.getDefault().getMaxNumberOfAttributes());
    attributes.forEach(attributesMap::put);
    return createTestSpan(
        Kind.INTERNAL,
        TraceConfig.getDefault(),
        null,
        attributesMap,
        Collections.singletonList(link));
  }

  private RecordEventsReadableSpan createTestRootSpan() {
    return createTestSpan(
        Kind.INTERNAL,
        TraceConfig.getDefault(),
        SpanId.getInvalid(),
        null,
        Collections.singletonList(link));
  }

  private RecordEventsReadableSpan createTestSpan(Kind kind) {
    return createTestSpan(
        kind, TraceConfig.getDefault(), parentSpanId, null, Collections.singletonList(link));
  }

  private RecordEventsReadableSpan createTestSpan(TraceConfig config) {
    return createTestSpan(
        Kind.INTERNAL, config, parentSpanId, null, Collections.singletonList(link));
  }

  private RecordEventsReadableSpan createTestSpan(
      Kind kind,
      TraceConfig config,
      @Nullable String parentSpanId,
      @Nullable AttributesMap attributes,
      List<Link> links) {

    RecordEventsReadableSpan span =
        RecordEventsReadableSpan.startSpan(
            spanContext,
            SPAN_NAME,
            instrumentationLibraryInfo,
            kind,
            parentSpanId,
            /* hasRemoteParent= */ true,
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
    testClock.advanceMillis(MILLIS_PER_SECOND);
    span.addEvent("event2", Attributes.empty());
    testClock.advanceMillis(MILLIS_PER_SECOND);
    span.updateName(SPAN_NEW_NAME);
    if (canonicalCode != null) {
      span.setStatus(canonicalCode, descriptio);
    }
  }

  private void verifySpanData(
      SpanData spanData,
      final ReadableAttributes attributes,
      List<Event> eventData,
      List<Link> links,
      String spanName,
      long startEpochNanos,
      long endEpochNanos,
      Status status,
      boolean hasEnded) {
    assertThat(spanData.getTraceId()).isEqualTo(traceId);
    assertThat(spanData.getSpanId()).isEqualTo(spanId);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.hasRemoteParent()).isEqualTo(EXPECTED_HAS_REMOTE_PARENT);
    assertThat(spanData.getTraceState()).isEqualTo(TraceState.getDefault());
    assertThat(spanData.getResource()).isEqualTo(resource);
    assertThat(spanData.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
    assertThat(spanData.getName()).isEqualTo(spanName);
    assertThat(spanData.getEvents()).isEqualTo(eventData);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getStartEpochNanos()).isEqualTo(startEpochNanos);
    assertThat(spanData.getEndEpochNanos()).isEqualTo(endEpochNanos);
    assertThat(spanData.getStatus().getCanonicalCode()).isEqualTo(status.getCanonicalCode());
    assertThat(spanData.hasEnded()).isEqualTo(hasEnded);

    // verify equality manually, since the implementations don't all equals with each other.
    ReadableAttributes spanDataAttributes = spanData.getAttributes();
    assertThat(spanDataAttributes.size()).isEqualTo(attributes.size());
    spanDataAttributes.forEach(
        new AttributeConsumer() {
          @Override
          public <T> void consume(AttributeKey<T> key, T value) {
            assertThat(attributes.get(key)).isEqualTo(value);
          }
        });
  }

  @Test
  void testAsSpanData() {
    String name = "GreatSpan";
    Kind kind = Kind.SERVER;
    String traceId = this.traceId;
    String spanId = this.spanId;
    String parentSpanId = this.parentSpanId;
    TraceConfig traceConfig = TraceConfig.getDefault();
    SpanProcessor spanProcessor = NoopSpanProcessor.getInstance();
    TestClock clock = TestClock.create();
    Resource resource = this.resource;
    Attributes attributes = TestUtils.generateRandomAttributes();
    final AttributesMap attributesWithCapacity = new AttributesMap(32);
    attributes.forEach(attributesWithCapacity::put);
    Attributes event1Attributes = TestUtils.generateRandomAttributes();
    Attributes event2Attributes = TestUtils.generateRandomAttributes();
    SpanContext context =
        SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
    Link link1 = Link.create(context, TestUtils.generateRandomAttributes());

    RecordEventsReadableSpan readableSpan =
        RecordEventsReadableSpan.startSpan(
            context,
            name,
            instrumentationLibraryInfo,
            kind,
            parentSpanId,
            /* hasRemoteParent= */ EXPECTED_HAS_REMOTE_PARENT,
            Context.root(),
            traceConfig,
            spanProcessor,
            clock,
            resource,
            attributesWithCapacity,
            Collections.singletonList(link1),
            1,
            0);
    long startEpochNanos = clock.now();
    clock.advanceMillis(4);
    long firstEventEpochNanos = clock.now();
    readableSpan.addEvent("event1", event1Attributes);
    clock.advanceMillis(6);
    long secondEventTimeNanos = clock.now();
    readableSpan.addEvent("event2", event2Attributes);

    clock.advanceMillis(100);
    readableSpan.end();
    long endEpochNanos = clock.now();

    List<Event> events =
        Arrays.asList(
            Event.create(firstEventEpochNanos, "event1", event1Attributes, event1Attributes.size()),
            Event.create(
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
        Status.unset(),
        /* hasEnded= */ true);
    assertThat(result.getTotalRecordedLinks()).isEqualTo(1);
    assertThat(result.isSampled()).isEqualTo(false);
  }

  @Test
  void testConcurrentModification() throws ExecutionException, InterruptedException {
    final RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
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
