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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.Attributes;
import io.opentelemetry.proto.trace.v1.Span.Links;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Span.TimedEvents;
import io.opentelemetry.resources.Resource;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceProtoUtils}. */
@RunWith(JUnit4.class)
public class TraceProtoUtilsTest {

  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final TraceId TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES, 0);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 0, 0, 0, 'b'};
  private static final SpanId SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES, 0);
  private static final Tracestate TRACESTATE = Tracestate.builder().set("foo", "bar").build();
  private static final Resource RESOURCE =
      Resource.create(Collections.<String, String>singletonMap("key", "val"));
  private static final Status STATUS = Status.DEADLINE_EXCEEDED.withDescription("TooSlow");
  private static final String ATTRIBUTE_KEY_1 = "MyAttributeKey1";
  private static final String ATTRIBUTE_KEY_2 = "MyAttributeKey2";

  private static final int DROPPED_ATTRIBUTES_COUNT = 1;
  private static final int DROPPED_EVENTS_COUNT = 3;
  private static final int DROPPED_LINKS_COUNT = 5;

  private static final Map<String, AttributeValue> attributes =
      ImmutableMap.of(
          ATTRIBUTE_KEY_1,
          AttributeValue.longAttributeValue(10L),
          ATTRIBUTE_KEY_2,
          AttributeValue.booleanAttributeValue(true));
  private static final List<TimedEvent> timedEvents =
      ImmutableList.of(TimedEvent.create(100, "event"));
  private static final List<Link> links =
      ImmutableList.of(
          io.opentelemetry.trace.util.Links.create(DefaultSpan.getInvalid().getContext()));

  private static final TestClock testClock = TestClock.create(Timestamp.getDefaultInstance());
  private static final TimestampConverter timestampConverter = TimestampConverter.now(testClock);

  @Test
  public void toProtoTraceId() {
    ByteString expected = ByteString.copyFrom(TRACE_ID_BYTES);
    assertThat(TraceProtoUtils.toProtoTraceId(TRACE_ID)).isEqualTo(expected);
  }

  @Test
  public void toProtoSpanId() {
    ByteString expected = ByteString.copyFrom(SPAN_ID_BYTES);
    assertThat(TraceProtoUtils.toProtoSpanId(SPAN_ID)).isEqualTo(expected);
  }

  @Test
  public void toProtoTracestate() {
    io.opentelemetry.proto.trace.v1.Span.Tracestate expected =
        io.opentelemetry.proto.trace.v1.Span.Tracestate.newBuilder()
            .addEntries(Span.Tracestate.Entry.newBuilder().setKey("foo").setValue("bar"))
            .build();
    assertThat(TraceProtoUtils.toProtoTracestate(TRACESTATE)).isEqualTo(expected);
  }

  @Test
  public void toProtoResource() {
    io.opentelemetry.proto.resource.v1.Resource expected =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder().putLabels("key", "val").build();
    assertThat(TraceProtoUtils.toProtoResource(RESOURCE)).isEqualTo(expected);
  }

  @Test
  public void toProtoKind() {
    assertThat(TraceProtoUtils.toProtoKind(Kind.CLIENT)).isEqualTo(SpanKind.CLIENT);
    assertThat(TraceProtoUtils.toProtoKind(Kind.SERVER)).isEqualTo(SpanKind.SERVER);
    assertThat(TraceProtoUtils.toProtoKind(Kind.CONSUMER)).isEqualTo(SpanKind.CONSUMER);
    assertThat(TraceProtoUtils.toProtoKind(Kind.PRODUCER)).isEqualTo(SpanKind.PRODUCER);
    assertThat(TraceProtoUtils.toProtoKind(Kind.INTERNAL)).isEqualTo(SpanKind.INTERNAL);
  }

  @Test
  public void toProtoAttributes() {
    Attributes expected =
        Attributes.newBuilder()
            .setDroppedAttributesCount(DROPPED_ATTRIBUTES_COUNT)
            .putAttributeMap(
                ATTRIBUTE_KEY_1,
                io.opentelemetry.proto.trace.v1.AttributeValue.newBuilder()
                    .setIntValue(10L)
                    .build())
            .putAttributeMap(
                ATTRIBUTE_KEY_2,
                io.opentelemetry.proto.trace.v1.AttributeValue.newBuilder()
                    .setBoolValue(true)
                    .build())
            .build();
    assertThat(TraceProtoUtils.toProtoAttributes(attributes, DROPPED_ATTRIBUTES_COUNT))
        .isEqualTo(expected);
  }

  @Test
  public void toProtoEvents() {
    TimedEvents expected =
        TimedEvents.newBuilder()
            .setDroppedTimedEventsCount(DROPPED_EVENTS_COUNT)
            .addTimedEvent(
                Span.TimedEvent.newBuilder()
                    .setTime(Timestamp.newBuilder().setNanos(100))
                    .setEvent(
                        Span.TimedEvent.Event.newBuilder()
                            .setAttributes(Attributes.getDefaultInstance())
                            .setName("event")))
            .build();
    assertThat(
            TraceProtoUtils.toProtoTimedEvents(
                timedEvents, DROPPED_EVENTS_COUNT, timestampConverter))
        .isEqualTo(expected);
  }

  @Test
  public void toProtoLinks() {
    Links expected =
        Links.newBuilder()
            .setDroppedLinksCount(DROPPED_LINKS_COUNT)
            .addLink(
                Span.Link.newBuilder()
                    .setTraceId(ByteString.copyFrom(new byte[TraceId.getSize()]))
                    .setSpanId(ByteString.copyFrom(new byte[SpanId.getSize()]))
                    .setTracestate(Span.Tracestate.getDefaultInstance())
                    .setAttributes(Attributes.newBuilder().build())
                    .build())
            .build();
    assertThat(TraceProtoUtils.toProtoLinks(links, DROPPED_LINKS_COUNT)).isEqualTo(expected);
  }

  @Test
  public void toProtoStatus() {
    io.opentelemetry.proto.trace.v1.Status expected1 =
        io.opentelemetry.proto.trace.v1.Status.newBuilder()
            .setCode(Status.DEADLINE_EXCEEDED.getCanonicalCode().value())
            .setMessage("TooSlow")
            .build();
    io.opentelemetry.proto.trace.v1.Status expected2 =
        io.opentelemetry.proto.trace.v1.Status.newBuilder()
            .setCode(Status.ABORTED.getCanonicalCode().value())
            .build();
    assertThat(TraceProtoUtils.toProtoStatus(STATUS)).isEqualTo(expected1);
    assertThat(TraceProtoUtils.toProtoStatus(Status.ABORTED)).isEqualTo(expected2);
  }

  @Test
  public void toProtoTimestamp() {
    Timestamp expected = Timestamp.newBuilder().setSeconds(123).setNanos(345678).build();
    assertThat(
            TraceProtoUtils.toProtoTimestamp(io.opentelemetry.trace.Timestamp.create(123, 345678)))
        .isEqualTo(expected);
  }
}
