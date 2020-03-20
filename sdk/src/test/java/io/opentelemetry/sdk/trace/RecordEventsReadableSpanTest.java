/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link RecordEventsReadableSpan}. */
@RunWith(JUnit4.class)
public class RecordEventsReadableSpanTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final String SPAN_NEW_NAME = "NewName";
  private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
  private static final long MILLIS_PER_SECOND = TimeUnit.SECONDS.toMillis(1);
  private final IdsGenerator idsGenerator = new RandomIdsGenerator();
  private final TraceId traceId = idsGenerator.generateTraceId();
  private final SpanId spanId = idsGenerator.generateSpanId();
  private final SpanId parentSpanId = idsGenerator.generateSpanId();
  private final boolean expectedHasRemoteParent = true;
  private final SpanContext spanContext =
      SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
  private final long startEpochNanos = 1000_123_789_654L;
  private final TestClock testClock = TestClock.create(startEpochNanos);
  private final Resource resource = Resource.getEmpty();
  private final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create("theName", null);
  private final Map<String, AttributeValue> attributes = new HashMap<>();
  private final Map<String, AttributeValue> expectedAttributes = new HashMap<>();
  private final Link link = SpanData.Link.create(spanContext);
  @Mock private SpanProcessor spanProcessor;
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    attributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    attributes.put("MyLongAttributeKey", AttributeValue.longAttributeValue(123L));
    attributes.put("MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(false));
    expectedAttributes.putAll(attributes);
    expectedAttributes.put(
        "MySingleStringAttributeKey",
        AttributeValue.stringAttributeValue("MySingleStringAttributeValue"));
  }

  @Test
  public void nothingChangedAfterEnd() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    span.end();
    // Check that adding trace events or update fields after Span#end() does not throw any thrown
    // and are ignored.
    spanDoWork(span, Status.CANCELLED);
    SpanData spanData = span.toSpanData();
    verifySpanData(
        spanData,
        Collections.<String, AttributeValue>emptyMap(),
        Collections.<SpanData.TimedEvent>emptyList(),
        Collections.singletonList(link),
        SPAN_NAME,
        startEpochNanos,
        startEpochNanos,
        Status.OK,
        /*hasEnded=*/ true);
  }

  @Test
  public void endSpanTwice_DoNotCrash() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    assertThat(span.hasEnded()).isFalse();
    span.end();
    assertThat(span.hasEnded()).isTrue();
    span.end();
    assertThat(span.hasEnded()).isTrue();
  }

  @Test
  public void toSpanData_ActiveSpan() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      assertThat(span.hasEnded()).isFalse();
      spanDoWork(span, null);
      SpanData spanData = span.toSpanData();
      SpanData.TimedEvent timedEvent =
          SpanData.TimedEvent.create(
              startEpochNanos + NANOS_PER_SECOND,
              "event2",
              Collections.<String, AttributeValue>emptyMap());
      verifySpanData(
          spanData,
          expectedAttributes,
          Collections.singletonList(timedEvent),
          Collections.singletonList(link),
          SPAN_NEW_NAME,
          startEpochNanos,
          0,
          Status.OK,
          /*hasEnded=*/ false);
      assertThat(span.hasEnded()).isFalse();
    } finally {
      span.end();
    }
    assertThat(span.hasEnded()).isTrue();
  }

  @Test
  public void toSpanData_EndedSpan() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      spanDoWork(span, Status.CANCELLED);
    } finally {
      span.end();
    }
    Mockito.verify(spanProcessor, Mockito.times(1)).onEnd(span);
    SpanData spanData = span.toSpanData();
    SpanData.TimedEvent timedEvent =
        SpanData.TimedEvent.create(
            startEpochNanos + NANOS_PER_SECOND,
            "event2",
            Collections.<String, AttributeValue>emptyMap());
    verifySpanData(
        spanData,
        expectedAttributes,
        Collections.singletonList(timedEvent),
        Collections.singletonList(link),
        SPAN_NEW_NAME,
        startEpochNanos,
        testClock.now(),
        Status.CANCELLED,
        /*hasEnded=*/ true);
  }

  @Test
  public void toSpanData_RootSpan() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      spanDoWork(span, null);
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertFalse(spanData.getParentSpanId().isValid());
  }

  @Test
  public void toSpanData_WithInitialAttributes() {
    RecordEventsReadableSpan span = createTestSpanWithAttributes(attributes);
    span.end();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(attributes.size());
  }

  @Test
  public void setStatus() {
    RecordEventsReadableSpan span = createTestSpan(Kind.CONSUMER);
    try {
      testClock.advanceMillis(MILLIS_PER_SECOND);
      assertThat(span.toSpanData().getStatus()).isEqualTo(Status.OK);
      span.setStatus(Status.CANCELLED);
      assertThat(span.toSpanData().getStatus()).isEqualTo(Status.CANCELLED);
    } finally {
      span.end();
    }
    assertThat(span.toSpanData().getStatus()).isEqualTo(Status.CANCELLED);
  }

  @Test
  public void getSpanKind() {
    RecordEventsReadableSpan span = createTestSpan(Kind.SERVER);
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(Kind.SERVER);
    } finally {
      span.end();
    }
  }

  @Test
  public void getInstrumentationLibraryInfo() {
    RecordEventsReadableSpan span = createTestSpan(Kind.CLIENT);
    try {
      assertThat(span.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
    } finally {
      span.end();
    }
  }

  @Test
  public void getSpanHasRemoteParent() {
    RecordEventsReadableSpan span = createTestSpan(Kind.SERVER);
    try {
      assertThat(span.toSpanData().getHasRemoteParent()).isTrue();
    } finally {
      span.end();
    }
  }

  @Test
  public void getAndUpdateSpanName() {
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
  public void getLatencyNs_ActiveSpan() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      testClock.advanceMillis(MILLIS_PER_SECOND);
      long elapsedTimeNanos1 = testClock.now() - startEpochNanos;
      assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos1);
      testClock.advanceMillis(MILLIS_PER_SECOND);
      long elapsedTimeNanos2 = testClock.now() - startEpochNanos;
      assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos2);
    } finally {
      span.end();
    }
  }

  @Test
  public void getLatencyNs_EndedSpan() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    testClock.advanceMillis(MILLIS_PER_SECOND);
    span.end();
    long elapsedTimeNanos = testClock.now() - startEpochNanos;
    assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos);
    testClock.advanceMillis(MILLIS_PER_SECOND);
    assertThat(span.getLatencyNanos()).isEqualTo(elapsedTimeNanos);
  }

  @Test
  public void setAttribute() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      span.setAttribute("StringKey", "StringVal");
      span.setAttribute("NullStringKey", (String) null);
      span.setAttribute("EmptyStringKey", "");
      span.setAttribute("NullStringAttributeValue", AttributeValue.stringAttributeValue(null));
      span.setAttribute("EmptyStringAttributeValue", AttributeValue.stringAttributeValue(""));
      span.setAttribute("LongKey", 1000L);
      span.setAttribute("DoubleKey", 10.0);
      span.setAttribute("BooleanKey", false);
      span.setAttribute(
          "ArrayStringKey",
          AttributeValue.arrayAttributeValue("StringVal", null, "", "StringVal2"));
      span.setAttribute("ArrayLongKey", AttributeValue.arrayAttributeValue(1L, 2L, 3L, 4L, 5L));
      span.setAttribute(
          "ArrayDoubleKey", AttributeValue.arrayAttributeValue(0.1, 2.3, 4.5, 6.7, 8.9));
      span.setAttribute(
          "ArrayBooleanKey", AttributeValue.arrayAttributeValue(true, false, false, true));
      span.setAttribute("NullArrayStringKey", AttributeValue.arrayAttributeValue((String[]) null));
      span.setAttribute("NullArrayLongKey", AttributeValue.arrayAttributeValue((Long[]) null));
      span.setAttribute("NullArrayDoubleKey", AttributeValue.arrayAttributeValue((Double[]) null));
      span.setAttribute(
          "NullArrayBooleanKey", AttributeValue.arrayAttributeValue((Boolean[]) null));
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(14);
    assertThat(spanData.getAttributes().get("ArrayStringKey").getStringArrayValue().size())
        .isEqualTo(4);
    assertThat(spanData.getAttributes().get("ArrayLongKey").getLongArrayValue().size())
        .isEqualTo(5);
    assertThat(spanData.getAttributes().get("ArrayDoubleKey").getDoubleArrayValue().size())
        .isEqualTo(5);
    assertThat(spanData.getAttributes().get("ArrayBooleanKey").getBooleanArrayValue().size())
        .isEqualTo(4);
  }

  @Test
  public void setAttribute_emptyArrayAttributeValue() throws Exception {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAttribute("stringArrayAttribute", AttributeValue.arrayAttributeValue(new String[0]));
    span.setAttribute("boolArrayAttribute", AttributeValue.arrayAttributeValue(new Boolean[0]));
    span.setAttribute("longArrayAttribute", AttributeValue.arrayAttributeValue(new Long[0]));
    span.setAttribute("doubleArrayAttribute", AttributeValue.arrayAttributeValue(new Double[0]));
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(4);
  }

  @Test
  public void setAttribute_nullStringValue() throws Exception {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAttribute("emptyString", "");
    span.setAttribute("nullString", (String) null);
    span.setAttribute("nullStringAttributeValue", AttributeValue.stringAttributeValue(null));
    span.setAttribute("emptyStringAttributeValue", AttributeValue.stringAttributeValue(""));
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
    span.setAttribute("emptyString", (String) null);
    span.setAttribute("emptyStringAttributeValue", (String) null);
    assertThat(span.toSpanData().getAttributes()).isEmpty();
  }

  @Test
  public void setAttribute_nullAttributeValue() throws Exception {
    RecordEventsReadableSpan span = createTestRootSpan();
    span.setAttribute("emptyString", "");
    span.setAttribute("nullString", (AttributeValue) null);
    span.setAttribute("nullStringAttributeValue", AttributeValue.stringAttributeValue(null));
    span.setAttribute("emptyStringAttributeValue", AttributeValue.stringAttributeValue(""));
    span.setAttribute("longAttribute", 0L);
    span.setAttribute("boolAttribute", false);
    span.setAttribute("doubleAttribute", 0.12345f);
    span.setAttribute("stringArrayAttribute", AttributeValue.arrayAttributeValue("", null));
    span.setAttribute("boolArrayAttribute", AttributeValue.arrayAttributeValue(true, null));
    span.setAttribute("longArrayAttribute", AttributeValue.arrayAttributeValue(12345L, null));
    span.setAttribute("doubleArrayAttribute", AttributeValue.arrayAttributeValue(1.2345, null));
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(9);
    span.setAttribute("emptyString", (AttributeValue) null);
    span.setAttribute("emptyStringAttributeValue", (AttributeValue) null);
    span.setAttribute("longAttribute", (AttributeValue) null);
    span.setAttribute("boolAttribute", (AttributeValue) null);
    span.setAttribute("doubleAttribute", (AttributeValue) null);
    span.setAttribute("stringArrayAttribute", (AttributeValue) null);
    span.setAttribute("boolArrayAttribute", (AttributeValue) null);
    span.setAttribute("longArrayAttribute", (AttributeValue) null);
    span.setAttribute("doubleArrayAttribute", (AttributeValue) null);
    assertThat(span.toSpanData().getAttributes()).isEmpty();
  }

  @Test
  public void addEvent() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      span.addEvent("event1");
      span.addEvent("event2", attributes);
      span.addEvent(
          new Event() {
            @Override
            public String getName() {
              return "event3";
            }

            @Override
            public Map<String, AttributeValue> getAttributes() {
              return Collections.emptyMap();
            }
          });
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getTimedEvents().size()).isEqualTo(3);
  }

  @Test
  public void droppingAttributes() {
    final int maxNumberOfAttributes = 8;
    TraceConfig traceConfig =
        TraceConfig.getDefault()
            .toBuilder()
            .setMaxNumberOfAttributes(maxNumberOfAttributes)
            .build();
    RecordEventsReadableSpan span = createTestSpan(traceConfig);
    try {
      for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
        span.setAttribute("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
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
  public void droppingAndAddingAttributes() {
    final int maxNumberOfAttributes = 8;
    TraceConfig traceConfig =
        TraceConfig.getDefault()
            .toBuilder()
            .setMaxNumberOfAttributes(maxNumberOfAttributes)
            .build();
    RecordEventsReadableSpan span = createTestSpan(traceConfig);
    try {
      for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
        span.setAttribute("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
      }
      SpanData spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      assertThat(spanData.getTotalAttributeCount()).isEqualTo(2 * maxNumberOfAttributes);

      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        int val = i + maxNumberOfAttributes * 3 / 2;
        span.setAttribute("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(val));
      }
      spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      // Test that we still have in the attributes map the latest maxNumberOfAttributes / 2 entries.
      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        int val = i + maxNumberOfAttributes * 3 / 2;
        AttributeValue expectedValue = AttributeValue.longAttributeValue(val);
        assertThat(spanData.getAttributes().get("MyStringAttributeKey" + i))
            .isEqualTo(expectedValue);
      }
      // Test that we have the newest re-added initial entries.
      for (int i = maxNumberOfAttributes / 2; i < maxNumberOfAttributes; i++) {
        AttributeValue expectedValue = AttributeValue.longAttributeValue(i);
        assertThat(spanData.getAttributes().get("MyStringAttributeKey" + i))
            .isEqualTo(expectedValue);
      }
    } finally {
      span.end();
    }
  }

  @Test
  public void droppingEvents() {
    final int maxNumberOfEvents = 8;
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder().setMaxNumberOfEvents(maxNumberOfEvents).build();
    RecordEventsReadableSpan span = createTestSpan(traceConfig);
    try {
      for (int i = 0; i < 2 * maxNumberOfEvents; i++) {
        span.addEvent("event2", Collections.<String, AttributeValue>emptyMap());
        testClock.advanceMillis(MILLIS_PER_SECOND);
      }
      SpanData spanData = span.toSpanData();

      assertThat(spanData.getTimedEvents().size()).isEqualTo(maxNumberOfEvents);
      for (int i = 0; i < maxNumberOfEvents; i++) {
        SpanData.TimedEvent expectedEvent =
            SpanData.TimedEvent.create(
                startEpochNanos + (maxNumberOfEvents + i) * NANOS_PER_SECOND,
                "event2",
                Collections.<String, AttributeValue>emptyMap());
        assertThat(spanData.getTimedEvents().get(i)).isEqualTo(expectedEvent);
        assertThat(spanData.getTotalRecordedEvents()).isEqualTo(2 * maxNumberOfEvents);
      }
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getTimedEvents().size()).isEqualTo(maxNumberOfEvents);
    for (int i = 0; i < maxNumberOfEvents; i++) {
      SpanData.TimedEvent expectedEvent =
          SpanData.TimedEvent.create(
              startEpochNanos + (maxNumberOfEvents + i) * NANOS_PER_SECOND,
              "event2",
              Collections.<String, AttributeValue>emptyMap());
      assertThat(spanData.getTimedEvents().get(i)).isEqualTo(expectedEvent);
    }
  }

  private RecordEventsReadableSpan createTestSpanWithAttributes(
      Map<String, AttributeValue> attributes) {
    return createTestSpan(Kind.INTERNAL, TraceConfig.getDefault(), null, attributes);
  }

  private RecordEventsReadableSpan createTestRootSpan() {
    return createTestSpan(
        Kind.INTERNAL,
        TraceConfig.getDefault(),
        null,
        Collections.<String, AttributeValue>emptyMap());
  }

  private RecordEventsReadableSpan createTestSpan(Kind kind) {
    return createTestSpan(
        kind,
        TraceConfig.getDefault(),
        parentSpanId,
        Collections.<String, AttributeValue>emptyMap());
  }

  private RecordEventsReadableSpan createTestSpan(TraceConfig config) {
    return createTestSpan(
        Kind.INTERNAL, config, parentSpanId, Collections.<String, AttributeValue>emptyMap());
  }

  private RecordEventsReadableSpan createTestSpan(
      Kind kind,
      TraceConfig config,
      @Nullable SpanId parentSpanId,
      Map<String, AttributeValue> attributes) {
    AttributesWithCapacity attributesWithCapacity =
        new AttributesWithCapacity(config.getMaxNumberOfAttributes());
    attributesWithCapacity.putAllAttributes(attributes);

    RecordEventsReadableSpan span =
        RecordEventsReadableSpan.startSpan(
            spanContext,
            SPAN_NAME,
            instrumentationLibraryInfo,
            kind,
            parentSpanId,
            /* hasRemoteParent= */ true,
            config,
            spanProcessor,
            testClock,
            resource,
            attributesWithCapacity,
            Collections.singletonList(link),
            1,
            0);
    Mockito.verify(spanProcessor, Mockito.times(1)).onStart(span);
    return span;
  }

  private void spanDoWork(RecordEventsReadableSpan span, @Nullable Status status) {
    span.setAttribute(
        "MySingleStringAttributeKey",
        AttributeValue.stringAttributeValue("MySingleStringAttributeValue"));
    for (Map.Entry<String, AttributeValue> attribute : attributes.entrySet()) {
      span.setAttribute(attribute.getKey(), attribute.getValue());
    }
    testClock.advanceMillis(MILLIS_PER_SECOND);
    span.addEvent("event2", Collections.<String, AttributeValue>emptyMap());
    testClock.advanceMillis(MILLIS_PER_SECOND);
    span.updateName(SPAN_NEW_NAME);
    if (status != null) {
      span.setStatus(status);
    }
  }

  private void verifySpanData(
      SpanData spanData,
      Map<String, AttributeValue> attributes,
      List<SpanData.TimedEvent> timedEvents,
      List<Link> links,
      String spanName,
      long startEpochNanos,
      long endEpochNanos,
      Status status,
      boolean hasEnded) {
    assertThat(spanData.getTraceId()).isEqualTo(traceId);
    assertThat(spanData.getSpanId()).isEqualTo(spanId);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getHasRemoteParent()).isEqualTo(expectedHasRemoteParent);
    assertThat(spanData.getTraceState()).isEqualTo(TraceState.getDefault());
    assertThat(spanData.getResource()).isEqualTo(resource);
    assertThat(spanData.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
    assertThat(spanData.getName()).isEqualTo(spanName);
    assertThat(spanData.getAttributes()).isEqualTo(attributes);
    assertThat(spanData.getTimedEvents()).isEqualTo(timedEvents);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getStartEpochNanos()).isEqualTo(startEpochNanos);
    assertThat(spanData.getEndEpochNanos()).isEqualTo(endEpochNanos);
    assertThat(spanData.getStatus().getCanonicalCode()).isEqualTo(status.getCanonicalCode());
    assertThat(spanData.getHasEnded()).isEqualTo(hasEnded);
  }

  @Test
  public void testAsSpanData() {
    String name = "GreatSpan";
    Kind kind = Kind.SERVER;
    TraceId traceId = idsGenerator.generateTraceId();
    SpanId spanId = idsGenerator.generateSpanId();
    SpanId parentSpanId = idsGenerator.generateSpanId();
    TraceConfig traceConfig = TraceConfig.getDefault();
    SpanProcessor spanProcessor = NoopSpanProcessor.getInstance();
    TestClock clock = TestClock.create();
    Map<String, AttributeValue> attribute = new HashMap<>();
    attribute.put("foo", AttributeValue.stringAttributeValue("bar"));
    Resource resource = Resource.create(attribute);
    Map<String, AttributeValue> attributes = TestUtils.generateRandomAttributes();
    AttributesWithCapacity attributesWithCapacity = new AttributesWithCapacity(32);
    attributesWithCapacity.putAllAttributes(attributes);
    Map<String, AttributeValue> event1Attributes = TestUtils.generateRandomAttributes();
    Map<String, AttributeValue> event2Attributes = TestUtils.generateRandomAttributes();
    SpanContext context =
        SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
    SpanData.Link link1 = SpanData.Link.create(context, TestUtils.generateRandomAttributes());

    RecordEventsReadableSpan readableSpan =
        RecordEventsReadableSpan.startSpan(
            context,
            name,
            instrumentationLibraryInfo,
            kind,
            parentSpanId,
            /* hasRemoteParent= */ false,
            traceConfig,
            spanProcessor,
            clock,
            resource,
            attributesWithCapacity,
            Collections.<Link>singletonList(link1),
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

    SpanData expected =
        SpanData.newBuilder()
            .setHasEnded(true)
            .setName(name)
            .setInstrumentationLibraryInfo(instrumentationLibraryInfo)
            .setKind(kind)
            .setStatus(Status.OK)
            .setStartEpochNanos(startEpochNanos)
            .setEndEpochNanos(endEpochNanos)
            .setTimedEvents(
                Arrays.asList(
                    SpanData.TimedEvent.create(firstEventEpochNanos, "event1", event1Attributes),
                    SpanData.TimedEvent.create(secondEventTimeNanos, "event2", event2Attributes)))
            .setTotalRecordedEvents(2)
            .setResource(resource)
            .setParentSpanId(parentSpanId)
            .setLinks(Collections.singletonList(link1))
            .setTotalRecordedLinks(1)
            .setTraceId(traceId)
            .setSpanId(spanId)
            .setAttributes(attributes)
            .setHasRemoteParent(false)
            .build();

    SpanData result = readableSpan.toSpanData();
    assertEquals(expected, result);
  }

  @Test
  public void testConcurrentModification() throws ExecutionException, InterruptedException {
    final RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<?> modifierFuture =
        es.submit(
            new Runnable() {
              @Override
              public void run() {
                for (int i = 0; i < 5096 * 5; ++i) {
                  span.setAttribute("hey" + i, "");
                }
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
