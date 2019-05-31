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
import io.opentelemetry.resources.Resource;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link RecordEventsSpanImpl}. */
@RunWith(JUnit4.class)
public class RecordEventsSpanImplTest {
  private static final String SPAN_NAME = "MySpanName";
  private final TraceId traceId = TestUtils.generateRandomTraceId();
  private final SpanId spanId = TestUtils.generateRandomSpanId();
  private final SpanId parentSpanId = TestUtils.generateRandomSpanId();
  private final SpanContext spanContext =
      SpanContext.create(traceId, spanId, TraceOptions.getDefault(), Tracestate.getDefault());
  private final Timestamp timestamp = Timestamp.newBuilder().setSeconds(1000).build();
  private final TestClock testClock = TestClock.create(timestamp);
  private final TimestampConverter timestampConverter = TimestampConverter.now(testClock);
  private final Resource resource = Resource.getEmpty();
  private final Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
  private final Map<String, AttributeValue> expectedAttributes =
      new HashMap<String, AttributeValue>();
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
  public void noEventsRecordedAfterEnd() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            Kind.INTERNAL,
            parentSpanId,
            TraceConfig.DEFAULT,
            spanProcessor,
            timestampConverter,
            testClock,
            resource);
    span.end();
    // Check that adding trace events after Span#end() does not throw any thrown and are not
    // recorded.
    span.setAttribute(
        "MySingleStringAttributeKey",
        AttributeValue.stringAttributeValue("MySingleStringAttributeValue"));
    span.addEvent("event");
    span.addLink(
        SpanData.Link.create(
            SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                TraceOptions.getDefault(),
                Tracestate.getDefault())));
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getStartTimestamp()).isEqualTo(toSpanDataTimestamp(timestamp));
    assertThat(spanData.getAttributes()).isEmpty();
    assertThat(spanData.getTimedEvents()).isEmpty();
    assertThat(spanData.getLinks()).isEmpty();
    assertThat(spanData.getStatus()).isEqualTo(Status.OK);
    assertThat(spanData.getEndTimestamp()).isEqualTo(toSpanDataTimestamp(timestamp));
  }

  @Test
  public void toSpanData_ActiveSpan() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            Kind.INTERNAL,
            parentSpanId,
            TraceConfig.DEFAULT,
            spanProcessor,
            timestampConverter,
            testClock,
            resource);
    thrown.expect(IllegalStateException.class);
    span.toSpanData();
  }

  @Test
  public void toSpanData_EndedSpan() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            Kind.INTERNAL,
            parentSpanId,
            TraceConfig.DEFAULT,
            spanProcessor,
            timestampConverter,
            testClock,
            resource);
    Mockito.verify(spanProcessor, Mockito.times(1)).onStartSync(span);
    span.setAttribute(
        "MySingleStringAttributeKey",
        AttributeValue.stringAttributeValue("MySingleStringAttributeValue"));
    for (Map.Entry<String, AttributeValue> attribute : attributes.entrySet()) {
      span.setAttribute(attribute.getKey(), attribute.getValue());
    }
    testClock.advanceMillis(1000);
    Event event = new SimpleEvent("event2", Collections.<String, AttributeValue>emptyMap());
    Event expectdEvent =
        SpanData.Event.create("event2", Collections.<String, AttributeValue>emptyMap());
    span.addEvent(event);
    Link link = SpanData.Link.create(spanContext);
    span.addLink(link);
    testClock.advanceMillis(1000);
    span.setStatus(Status.CANCELLED);
    span.end();
    Mockito.verify(spanProcessor, Mockito.times(1)).onEndSync(span);
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getAttributes()).isEqualTo(expectedAttributes);
    assertThat(spanData.getTimedEvents().size()).isEqualTo(1);
    assertThat(spanData.getTimedEvents().get(0).getEvent()).isEqualTo(expectdEvent);
    assertThat(spanData.getLinks().size()).isEqualTo(1);
    assertThat(spanData.getLinks().get(0)).isEqualTo(link);
    assertThat(spanData.getStartTimestamp()).isEqualTo(toSpanDataTimestamp(timestamp));
    assertThat(spanData.getStatus()).isEqualTo(Status.CANCELLED);
    assertThat(spanData.getEndTimestamp()).isEqualTo(toSpanDataTimestamp(testClock.now()));
  }

  @Test
  public void status_ViaSetStatus() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            Kind.INTERNAL,
            parentSpanId,
            TraceConfig.DEFAULT,
            spanProcessor,
            timestampConverter,
            testClock,
            resource);
    Mockito.verify(spanProcessor, Mockito.times(1)).onStartSync(span);
    testClock.advanceMillis(10);
    assertThat(span.getStatus()).isEqualTo(Status.OK);
    span.setStatus(Status.CANCELLED);
    assertThat(span.getStatus()).isEqualTo(Status.CANCELLED);
    span.end();
    assertThat(span.getStatus()).isEqualTo(Status.CANCELLED);
  }

  @Test
  public void getSpanKind() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            Kind.SERVER,
            parentSpanId,
            TraceConfig.DEFAULT,
            spanProcessor,
            timestampConverter,
            testClock,
            resource);
    assertThat(span.getKind()).isEqualTo(Kind.SERVER);
  }

  private static SpanData.Timestamp toSpanDataTimestamp(Timestamp protoTimestamp) {
    return SpanData.Timestamp.create(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
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
