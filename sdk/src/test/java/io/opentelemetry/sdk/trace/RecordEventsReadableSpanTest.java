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

import io.opentelemetry.common.Timestamp;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import io.opentelemetry.trace.util.Events;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final TraceId traceId = TestUtils.generateRandomTraceId();
  private final SpanId spanId = TestUtils.generateRandomSpanId();
  private final SpanId parentSpanId = TestUtils.generateRandomSpanId();
  private final SpanContext spanContext =
      SpanContext.create(traceId, spanId, TraceFlags.getDefault(), Tracestate.getDefault());
  private final com.google.protobuf.Timestamp startTime =
      com.google.protobuf.Timestamp.newBuilder().setSeconds(1000).build();
  private final TestClock testClock = TestClock.create(startTime);
  private final TimestampConverter timestampConverter = TimestampConverter.now(testClock);
  private final Resource resource = Resource.getEmpty();
  private final Map<String, AttributeValue> attributes = new HashMap<>();
  private final Map<String, AttributeValue> expectedAttributes = new HashMap<>();
  private final Event event =
      new SimpleEvent("event2", Collections.<String, AttributeValue>emptyMap());
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
        Timestamp.create(startTime.getSeconds(), 0),
        Timestamp.create(startTime.getSeconds(), 0),
        Status.OK);
  }

  @Test
  public void endSpanTwice_DoNotCrash() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    span.end();
    span.end();
  }

  @Test
  public void toSpanData_ActiveSpan() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      spanDoWork(span, null);
      SpanData spanData = span.toSpanData();
      SpanData.TimedEvent timedEvent =
          SpanData.TimedEvent.create(Timestamp.create(startTime.getSeconds() + 1, 0), event);
      verifySpanData(
          spanData,
          expectedAttributes,
          Collections.<SpanData.TimedEvent>singletonList(timedEvent),
          Collections.<Link>singletonList(link),
          SPAN_NEW_NAME,
          Timestamp.create(startTime.getSeconds(), 0),
          Timestamp.create(testClock.now().getSeconds(), 0),
          Status.OK);
    } finally {
      span.end();
    }
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
        SpanData.TimedEvent.create(Timestamp.create(startTime.getSeconds() + 1, 0), event);
    verifySpanData(
        spanData,
        expectedAttributes,
        Collections.<SpanData.TimedEvent>singletonList(timedEvent),
        Collections.singletonList(link),
        SPAN_NEW_NAME,
        Timestamp.create(startTime.getSeconds(), 0),
        Timestamp.create(testClock.now().getSeconds(), 0),
        Status.CANCELLED);
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
      assertThat(span.getStatus()).isEqualTo(Status.OK);
      span.setStatus(Status.CANCELLED);
      assertThat(span.getStatus()).isEqualTo(Status.CANCELLED);
    } finally {
      span.end();
    }
    assertThat(span.getStatus()).isEqualTo(Status.CANCELLED);
  }

  @Test
  public void getSpanKind() {
    RecordEventsReadableSpan span = createTestSpan(Kind.SERVER);
    try {
      assertThat(span.getKind()).isEqualTo(Kind.SERVER);
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
      long elapsedTimeNanos1 =
          (testClock.now().getSeconds() - startTime.getSeconds()) * NANOS_PER_SECOND;
      assertThat(span.getLatencyNs()).isEqualTo(elapsedTimeNanos1);
      testClock.advanceMillis(MILLIS_PER_SECOND);
      long elapsedTimeNanos2 =
          (testClock.now().getSeconds() - startTime.getSeconds()) * NANOS_PER_SECOND;
      assertThat(span.getLatencyNs()).isEqualTo(elapsedTimeNanos2);
    } finally {
      span.end();
    }
  }

  @Test
  public void getLatencyNs_EndedSpan() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    testClock.advanceMillis(MILLIS_PER_SECOND);
    span.end();
    long elapsedTimeNanos =
        (testClock.now().getSeconds() - startTime.getSeconds()) * NANOS_PER_SECOND;
    assertThat(span.getLatencyNs()).isEqualTo(elapsedTimeNanos);
    testClock.advanceMillis(MILLIS_PER_SECOND);
    assertThat(span.getLatencyNs()).isEqualTo(elapsedTimeNanos);
  }

  @Test
  public void setAttribute() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      span.setAttribute("StringKey", "StringVal");
      span.setAttribute("LongKey", 1000L);
      span.setAttribute("DoubleKey", 10.0);
      span.setAttribute("BooleanKey", false);
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(4);
  }

  @Test
  public void addEvent() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      span.addEvent("event1");
      span.addEvent("event2", attributes);
      span.addEvent(Events.create("event3"));
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
      for (int i = 0; i < maxNumberOfAttributes; i++) {
        AttributeValue expectedValue = AttributeValue.longAttributeValue(i + maxNumberOfAttributes);
        assertThat(
                spanData.getAttributes().get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
            .isEqualTo(expectedValue);
      }
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
    for (int i = 0; i < maxNumberOfAttributes; i++) {
      AttributeValue expectedValue = AttributeValue.longAttributeValue(i + maxNumberOfAttributes);
      assertThat(spanData.getAttributes().get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
          .isEqualTo(expectedValue);
    }
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
      for (int i = 0; i < maxNumberOfAttributes; i++) {
        AttributeValue expectedValue = AttributeValue.longAttributeValue(i + maxNumberOfAttributes);
        assertThat(
                spanData.getAttributes().get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
            .isEqualTo(expectedValue);
      }

      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        span.setAttribute("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
      }
      spanData = span.toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      // Test that we still have in the attributes map the latest maxNumberOfAttributes / 2 entries.
      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        int val = i + maxNumberOfAttributes * 3 / 2;
        AttributeValue expectedValue = AttributeValue.longAttributeValue(val);
        assertThat(spanData.getAttributes().get("MyStringAttributeKey" + val))
            .isEqualTo(expectedValue);
      }
      // Test that we have the newest re-added initial entries.
      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
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
        span.addEvent(event);
        testClock.advanceMillis(MILLIS_PER_SECOND);
      }
      SpanData spanData = span.toSpanData();

      assertThat(spanData.getTimedEvents().size()).isEqualTo(maxNumberOfEvents);
      for (int i = 0; i < maxNumberOfEvents; i++) {
        SpanData.TimedEvent expectedEvent =
            SpanData.TimedEvent.create(
                Timestamp.create(startTime.getSeconds() + maxNumberOfEvents + i, 0), event);
        assertThat(spanData.getTimedEvents().get(i)).isEqualTo(expectedEvent);
      }
    } finally {
      span.end();
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getTimedEvents().size()).isEqualTo(maxNumberOfEvents);
    for (int i = 0; i < maxNumberOfEvents; i++) {
      SpanData.TimedEvent expectedEvent =
          SpanData.TimedEvent.create(
              Timestamp.create(startTime.getSeconds() + maxNumberOfEvents + i, 0), event);
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
    RecordEventsReadableSpan span =
        RecordEventsReadableSpan.startSpan(
            spanContext,
            SPAN_NAME,
            kind,
            parentSpanId,
            config,
            spanProcessor,
            timestampConverter,
            testClock,
            resource,
            attributes,
            Collections.singletonList(link),
            1);
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
    span.addEvent(event);
    testClock.advanceMillis(MILLIS_PER_SECOND);
    span.addChild();
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
      Timestamp startTime,
      Timestamp endTime,
      Status status) {
    assertThat(spanData.getTraceId()).isEqualTo(traceId);
    assertThat(spanData.getSpanId()).isEqualTo(spanId);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getTracestate()).isEqualTo(Tracestate.getDefault());
    assertThat(spanData.getResource()).isEqualTo(resource);
    assertThat(spanData.getName()).isEqualTo(spanName);
    assertThat(spanData.getAttributes()).isEqualTo(attributes);
    assertThat(spanData.getTimedEvents()).isEqualTo(timedEvents);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getStartTimestamp()).isEqualTo(startTime);
    assertThat(spanData.getEndTimestamp()).isEqualTo(endTime);
    assertThat(spanData.getStatus().getCanonicalCode()).isEqualTo(status.getCanonicalCode());
  }

  private static final class SimpleEvent implements Event {

    private final String name;
    private final Map<String, AttributeValue> attributes;

    private SimpleEvent(String name, Map<String, AttributeValue> attributes) {
      this.name = name;
      this.attributes = attributes;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Map<String, AttributeValue> getAttributes() {
      return attributes;
    }
  }

  @Test
  public void testAsSpanData() {
    String name = "GreatSpan";
    Kind kind = Kind.SERVER;
    TraceId traceId = TestUtils.generateRandomTraceId();
    SpanId spanId = TestUtils.generateRandomSpanId();
    SpanId parentSpanId = TestUtils.generateRandomSpanId();
    TraceConfig traceConfig = TraceConfig.getDefault();
    SpanProcessor spanProcessor = NoopSpanProcessor.getInstance();
    TestClock clock = TestClock.create();
    Map<String, String> labels = new HashMap<>();
    labels.put("foo", "bar");
    Resource resource = Resource.create(labels);
    Map<String, AttributeValue> attributes = TestUtils.generateRandomAttributes();
    Map<String, AttributeValue> event1Attributes = TestUtils.generateRandomAttributes();
    Map<String, AttributeValue> event2Attributes = TestUtils.generateRandomAttributes();
    SpanContext context =
        SpanContext.create(traceId, spanId, TraceFlags.getDefault(), Tracestate.getDefault());
    Link link1 =
        io.opentelemetry.trace.util.Links.create(context, TestUtils.generateRandomAttributes());
    List<Link> links = Collections.singletonList(link1);

    RecordEventsReadableSpan readableSpan =
        RecordEventsReadableSpan.startSpan(
            context,
            name,
            kind,
            parentSpanId,
            traceConfig,
            spanProcessor,
            null,
            clock,
            resource,
            attributes,
            links,
            1);
    long startTimeNanos = clock.nowNanos();
    clock.advanceMillis(4);
    long firstEventTimeNanos = clock.nowNanos();
    readableSpan.addEvent("event1", event1Attributes);
    clock.advanceMillis(6);
    long secondEventTimeNanos = clock.nowNanos();
    readableSpan.addEvent("event2", event2Attributes);

    clock.advanceMillis(100);
    readableSpan.end();
    long endTimeNanos = clock.nowNanos();

    SpanData expected =
        SpanData.newBuilder()
            .setName(name)
            .setKind(kind)
            .setStatus(Status.OK)
            .setStartTimestamp(nanoToTimestamp(startTimeNanos))
            .setEndTimestamp(nanoToTimestamp(endTimeNanos))
            .setTimedEvents(
                Arrays.asList(
                    SpanData.TimedEvent.create(
                        nanoToTimestamp(firstEventTimeNanos),
                        Events.create("event1", event1Attributes)),
                    SpanData.TimedEvent.create(
                        nanoToTimestamp(secondEventTimeNanos),
                        Events.create("event2", event2Attributes))))
            .setResource(resource)
            .setParentSpanId(parentSpanId)
            .setLinks(links)
            .setTraceId(traceId)
            .setSpanId(spanId)
            .setAttributes(attributes)
            .build();

    SpanData result = readableSpan.toSpanData();
    assertEquals(expected, result);
  }

  private static Timestamp nanoToTimestamp(long nanotime) {
    return Timestamp.create(nanotime / NANOS_PER_SECOND, (int) (nanotime % NANOS_PER_SECOND));
  }
}
