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

import static org.junit.Assert.assertEquals;

import io.opentelemetry.sdk.internal.Clock;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.sdk.trace.export.SpanData.Event;
import io.opentelemetry.sdk.trace.export.SpanData.Timestamp;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReadableSpanAdapterTest {
  private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);

  @Test
  public void testAdapt() {
    String name = "GreatSpan";
    Kind kind = Kind.SERVER;
    TraceId traceId = TestUtils.generateRandomTraceId();
    SpanId spanId = TestUtils.generateRandomSpanId();
    SpanId parentSpanId = TestUtils.generateRandomSpanId();
    TraceConfig traceConfig = TraceConfig.getDefault();
    SpanProcessor spanProcessor = NoopSpanProcessor.getInstance();
    Clock clock = TestClock.create();
    Map<String, String> labels = new HashMap<>();
    labels.put("foo", "bar");
    Resource resource = Resource.create(labels);
    Map<String, AttributeValue> attributes = TestUtils.generateRandomAttributes();
    Map<String, AttributeValue> event1Attributes = TestUtils.generateRandomAttributes();
    Map<String, AttributeValue> event2Attributes = TestUtils.generateRandomAttributes();
    SpanContext context =
        SpanContext.create(traceId, spanId, TraceFlags.getDefault(), Tracestate.getDefault());
    Link link1 = SpanData.Link.create(context, TestUtils.generateRandomAttributes());
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
    readableSpan.addEvent("event1", event1Attributes);
    readableSpan.addEvent("event2", event2Attributes);
    readableSpan.end();

    SpanData expected =
        SpanData.newBuilder()
            .name(name)
            .kind(kind)
            .status(Status.OK)
            .startTimestamp(nanoToTimestamp(readableSpan.getStartNanoTime()))
            .endTimestamp(nanoToTimestamp(readableSpan.getEndNanoTime()))
            .timedEvents(
                Arrays.asList(
                    SpanData.TimedEvent.create(
                        nanoToTimestamp(clock.nowNanos()),
                        Event.create("event1", event1Attributes)),
                    SpanData.TimedEvent.create(
                        nanoToTimestamp(clock.nowNanos()),
                        Event.create("event2", event2Attributes))))
            .resource(resource)
            .parentSpanId(parentSpanId)
            .links(links)
            .context(context)
            .attributes(attributes)
            .build();

    ReadableSpanAdapter testClass = new ReadableSpanAdapter();
    SpanData result = testClass.adapt(readableSpan);
    assertEquals(expected, result);
  }

  private static Timestamp nanoToTimestamp(long nanotime) {
    return Timestamp.create(nanotime / NANOS_PER_SECOND, (int) (nanotime % NANOS_PER_SECOND));
  }
}
