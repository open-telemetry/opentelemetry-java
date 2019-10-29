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

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import io.opentelemetry.sdk.trace.SpanData.TimedEvent;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
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

    assertFalse(spanData.getParentSpanId().isValid());
    assertEquals(Collections.<String, AttributeValue>emptyMap(), spanData.getAttributes());
    assertEquals(emptyList(), spanData.getTimedEvents());
    assertEquals(emptyList(), spanData.getLinks());
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

  private static SpanData createSpanDataWithMutableCollections() {
    return createBasicSpanBuilder()
        .setLinks(new ArrayList<Link>())
        .setTimedEvents(new ArrayList<TimedEvent>())
        .setAttributes(new HashMap<String, AttributeValue>())
        .build();
  }

  private static Link emptyLink() {
    return SpanData.Link.create(
        SpanContext.create(
            TraceId.getInvalid(),
            SpanId.getInvalid(),
            TraceFlags.getDefault(),
            Tracestate.getDefault()));
  }

  private static SpanData.Builder createBasicSpanBuilder() {
    return SpanData.newBuilder()
        .setSpanId(SpanId.getInvalid())
        .setTraceId(TraceId.getInvalid())
        .setName("spanName")
        .setStartEpochNanos(START_EPOCH_NANOS)
        .setEndEpochNanos(END_EPOCH_NANOS)
        .setKind(Kind.SERVER)
        .setStatus(Status.OK);
  }
}
