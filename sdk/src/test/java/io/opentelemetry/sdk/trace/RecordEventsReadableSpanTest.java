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

import com.google.protobuf.Timestamp;
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
  private final Timestamp startTime = Timestamp.newBuilder().setSeconds(1000).build();
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
    verifySpan(
        span,
        Collections.<String, AttributeValue>emptyMap(),
        Collections.<TimedEvent>emptyList(),
        Collections.<Link>singletonList(link),
        SPAN_NAME,
        startTime,
        startTime,
        Status.OK,
        0);
  }

  @Test
  public void endSpanTwice_DoNotCrash() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    span.end();
    span.end();
  }

  @Test
  public void activeSpanState() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      spanDoWork(span, null);
      long timeInNanos = (startTime.getSeconds() + 1) * NANOS_PER_SECOND;
      TimedEvent timedEvent = TimedEvent.create(timeInNanos, event);
      verifySpan(
          span,
          expectedAttributes,
          Collections.singletonList(timedEvent),
          Collections.singletonList(link),
          SPAN_NEW_NAME,
          startTime,
          testClock.now(),
          Status.OK,
          1);
    } finally {
      span.end();
    }
  }

  @Test
  public void endedSpanState() {
    RecordEventsReadableSpan span = createTestSpan(Kind.INTERNAL);
    try {
      spanDoWork(span, Status.CANCELLED);
    } finally {
      span.end();
    }
    Mockito.verify(spanProcessor, Mockito.times(1)).onEnd(span);
    long timeInNanos = (startTime.getSeconds() + 1) * NANOS_PER_SECOND;
    TimedEvent timedEvent = TimedEvent.create(timeInNanos, event);
    verifySpan(
        span,
        expectedAttributes,
        Collections.singletonList(timedEvent),
        Collections.singletonList(link),
        SPAN_NEW_NAME,
        startTime,
        testClock.now(),
        Status.CANCELLED,
        1);
  }

  @Test
  public void rootSpan() {
    RecordEventsReadableSpan span = createTestRootSpan();
    try {
      spanDoWork(span, null);
    } finally {
      span.end();
    }
    assertThat(span.getParentSpanId().isValid()).isFalse();
  }

  @Test
  public void initialAttributes() {
    RecordEventsReadableSpan span = createTestSpanWithAttributes(attributes);
    span.end();
    assertThat(span.getAttributes().size()).isEqualTo(attributes.size());
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
    assertThat(span.getAttributes().size()).isEqualTo(4);
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
    assertThat(span.getTimedEvents().size()).isEqualTo(3);
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
      assertThat(span.getDroppedAttributesCount()).isEqualTo(maxNumberOfAttributes);
      assertThat(span.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      for (int i = 0; i < maxNumberOfAttributes; i++) {
        AttributeValue expectedValue = AttributeValue.longAttributeValue(i + maxNumberOfAttributes);
        assertThat(span.getAttributes().get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
            .isEqualTo(expectedValue);
      }
    } finally {
      span.end();
    }
    assertThat(span.getDroppedAttributesCount()).isEqualTo(maxNumberOfAttributes);
    assertThat(span.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
    for (int i = 0; i < maxNumberOfAttributes; i++) {
      AttributeValue expectedValue = AttributeValue.longAttributeValue(i + maxNumberOfAttributes);
      assertThat(span.getAttributes().get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
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
      assertThat(span.getDroppedAttributesCount()).isEqualTo(maxNumberOfAttributes);
      assertThat(span.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      for (int i = 0; i < maxNumberOfAttributes; i++) {
        AttributeValue expectedValue = AttributeValue.longAttributeValue(i + maxNumberOfAttributes);
        assertThat(span.getAttributes().get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
            .isEqualTo(expectedValue);
      }

      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        span.setAttribute("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
      }
      assertThat(span.getAttributes().size()).isEqualTo(maxNumberOfAttributes);
      assertThat(span.getRawAttributes().getNumberOfDroppedAttributes())
          .isEqualTo(maxNumberOfAttributes * 3 / 2);
      // Test that we still have in the attributes map the latest maxNumberOfAttributes / 2 entries.
      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        int val = i + maxNumberOfAttributes * 3 / 2;
        AttributeValue expectedValue = AttributeValue.longAttributeValue(val);
        assertThat(span.getAttributes().get("MyStringAttributeKey" + val)).isEqualTo(expectedValue);
      }
      // Test that we have the newest re-added initial entries.
      for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
        AttributeValue expectedValue = AttributeValue.longAttributeValue(i);
        assertThat(span.getAttributes().get("MyStringAttributeKey" + i)).isEqualTo(expectedValue);
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
      assertThat(span.getDroppedTimedEventsCount()).isEqualTo(maxNumberOfEvents);
      assertThat(span.getTimedEvents().size()).isEqualTo(maxNumberOfEvents);
      for (int i = 0; i < maxNumberOfEvents; i++) {
        long timeInNanos = (startTime.getSeconds() + maxNumberOfEvents + i) * NANOS_PER_SECOND;
        assertThat(span.getTimedEvents().get(i)).isEqualTo(TimedEvent.create(timeInNanos, event));
      }
    } finally {
      span.end();
    }
    assertThat(span.getDroppedTimedEventsCount()).isEqualTo(maxNumberOfEvents);
    assertThat(span.getTimedEvents().size()).isEqualTo(maxNumberOfEvents);
    for (int i = 0; i < maxNumberOfEvents; i++) {
      long timeInNanos = (startTime.getSeconds() + maxNumberOfEvents + i) * NANOS_PER_SECOND;
      assertThat(span.getTimedEvents().get(i)).isEqualTo(TimedEvent.create(timeInNanos, event));
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

  private void verifySpan(
      ReadableSpan span,
      Map<String, AttributeValue> attributes,
      List<TimedEvent> timedEvents,
      List<Link> links,
      String spanName,
      Timestamp startTime,
      Timestamp endTime,
      Status status,
      int childCount) {
    assertThat(span.getSpanContext().getTraceId()).isEqualTo(traceId);
    assertThat(span.getSpanContext().getSpanId()).isEqualTo(spanId);
    assertThat(span.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(span.getSpanContext().getTracestate()).isEqualTo(Tracestate.getDefault());
    assertThat(span.getResource()).isEqualTo(resource);
    assertThat(span.getName()).isEqualTo(spanName);
    assertThat(span.getAttributes()).isEqualTo(attributes);
    assertThat(span.getTimedEvents()).isEqualTo(timedEvents);
    assertThat(span.getLinks()).isEqualTo(links);
    assertThat(span.getStartNanoTime())
        .isEqualTo(startTime.getSeconds() * 1_000_000_000 + startTime.getNanos());
    assertThat(span.getEndNanoTime())
        .isEqualTo(endTime.getSeconds() * 1_000_000_000 + endTime.getNanos());
    assertThat(span.getStatus().getCanonicalCode()).isEqualTo(status.getCanonicalCode());
    assertThat(span.getChildSpanCount()).isEqualTo(childCount);
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
}
