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

package io.opentelemetry.sdk.trace.data;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.emptyList;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.trace.data.SpanData.TimedEvent;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpanData}. */
@RunWith(JUnit4.class)
public class SpanDataTest {

  private static final long START_EPOCH_NANOS = TimeUnit.SECONDS.toNanos(3000) + 200;
  private static final long END_EPOCH_NANOS = TimeUnit.SECONDS.toNanos(3001) + 255;
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultValues() {
    SpanData spanData = createBasicSpanBuilder().build();

    assertThat(spanData.getParentSpanId().isValid()).isFalse();
    assertThat(spanData.getAttributes()).isEqualTo(Collections.<String, AttributeValue>emptyMap());
    assertThat(spanData.getTimedEvents()).isEqualTo(emptyList());
    assertThat(spanData.getLinks()).isEqualTo(emptyList());
    assertThat(spanData.getInstrumentationLibraryInfo())
        .isSameInstanceAs(InstrumentationLibraryInfo.getEmpty());
    assertThat(spanData.getHasRemoteParent()).isFalse();
  }

  @Test
  public void unmodifiableAttributes() {
    SpanData spanData = createSpanDataWithMutableCollections();

    thrown.expect(UnsupportedOperationException.class);
    spanData.getAttributes().put("foo", AttributeValue.longAttributeValue(555));
  }

  @Test
  public void unmodifiableLinks() {
    SpanData spanData = createSpanDataWithMutableCollections();

    thrown.expect(UnsupportedOperationException.class);
    spanData.getLinks().add(emptyLink());
  }

  @Test
  public void unmodifiableTimedEvents() {
    SpanData spanData = createSpanDataWithMutableCollections();

    thrown.expect(UnsupportedOperationException.class);
    spanData
        .getTimedEvents()
        .add(TimedEvent.create(1234, "foo", Collections.<String, AttributeValue>emptyMap()));
  }

  @Test
  public void defaultTotalAttributeCountIsZero() {
    SpanData spanData = createSpanDataWithMutableCollections();
    assertThat(spanData.getTotalAttributeCount()).isEqualTo(0);
  }

  @Test
  public void canSetTotalAttributeCountWithBuilder() {
    SpanData spanData = createBasicSpanBuilder().setTotalAttributeCount(123).build();
    assertThat(spanData.getTotalAttributeCount()).isEqualTo(123);
  }

  @Test
  public void link_defaultTotalAttributeCountIsZero() {
    SpanData.Link link = SpanData.Link.create(SpanContext.getInvalid());
    assertThat(link.getTotalAttributeCount()).isEqualTo(0);
  }

  @Test
  public void link_canSetTotalAttributeCount() {
    SpanData.Link link = SpanData.Link.create(SpanContext.getInvalid());
    assertThat(link.getTotalAttributeCount()).isEqualTo(0);
  }

  @Test
  public void timedEvent_defaultTotalAttributeCountIsZero() {
    SpanData.TimedEvent event =
        SpanData.TimedEvent.create(
            START_EPOCH_NANOS, "foo", Collections.<String, AttributeValue>emptyMap());
    assertThat(event.getTotalAttributeCount()).isEqualTo(0);
  }

  @Test
  public void timedEvent_canSetTotalAttributeCount() {
    SpanData.TimedEvent event =
        SpanData.TimedEvent.create(
            START_EPOCH_NANOS, "foo", Collections.<String, AttributeValue>emptyMap(), 123);
    assertThat(event.getTotalAttributeCount()).isEqualTo(123);
  }

  private static SpanData createSpanDataWithMutableCollections() {
    return createBasicSpanBuilder()
        .setLinks(new ArrayList<SpanData.Link>())
        .setTimedEvents(new ArrayList<TimedEvent>())
        .setAttributes(new HashMap<String, AttributeValue>())
        .build();
  }

  private static SpanData.Link emptyLink() {
    return SpanData.Link.create(SpanContext.getInvalid());
  }

  private static SpanData.Builder createBasicSpanBuilder() {
    return SpanData.newBuilder()
        .setHasEnded(true)
        .setSpanId(SpanId.getInvalid())
        .setTraceId(TraceId.getInvalid())
        .setName("spanName")
        .setStartEpochNanos(START_EPOCH_NANOS)
        .setEndEpochNanos(END_EPOCH_NANOS)
        .setKind(Kind.SERVER)
        .setStatus(Status.OK)
        .setHasRemoteParent(false)
        .setTotalRecordedEvents(0)
        .setTotalRecordedLinks(0);
  }
}
