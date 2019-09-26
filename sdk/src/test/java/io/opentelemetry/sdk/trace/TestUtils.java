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

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanData;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Common utilities for unit tests. */
public final class TestUtils {

  private TestUtils() {}

  /**
   * Returns a random {@link TraceId}.
   *
   * @return a random {@link TraceId}.
   */
  public static TraceId generateRandomTraceId() {
    return TraceId.fromLowerBase16(UUID.randomUUID().toString().replace("-", ""), 0);
  }

  /**
   * Returns a random {@link SpanId}.
   *
   * @return a random {@link SpanId}.
   */
  public static SpanId generateRandomSpanId() {
    return SpanId.fromLowerBase16(UUID.randomUUID().toString().replace("-", ""), 0);
  }

  /**
   * Generates some random attributes used for testing.
   *
   * @return a map of String to AttributeValues
   */
  static Map<String, AttributeValue> generateRandomAttributes() {
    Map<String, AttributeValue> result = new HashMap<>();
    AttributeValue attribute = AttributeValue.stringAttributeValue(UUID.randomUUID().toString());
    result.put(UUID.randomUUID().toString(), attribute);
    return result;
  }

  /**
   * Create a very basic SpanData instance, suitable for testing. It has the bare minimum viable
   * data.
   *
   * @return A SpanData instance.
   */
  public static SpanData makeBasicSpan() {
    return SpanData.newBuilder()
        .context(
            SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                TraceFlags.builder().build(),
                Tracestate.builder().build()))
        .parentSpanId(SpanId.getInvalid())
        .name("span")
        .kind(Kind.SERVER)
        .resource(Resource.create(Collections.<String, String>emptyMap()))
        .startTimestamp(Timestamp.create(100, 100))
        .attributes(Collections.<String, AttributeValue>emptyMap())
        .timedEvents(Collections.<SpanData.TimedEvent>emptyList())
        .links(Collections.<Link>emptyList())
        .status(Status.OK)
        .endTimestamp(Timestamp.create(200, 200))
        .build();
  }
}
