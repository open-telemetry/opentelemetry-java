/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.trace;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.Attribute;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.BooleanValuedKey;
import io.opentelemetry.common.LongValuedKey;
import io.opentelemetry.common.MapBasedAttributes;
import io.opentelemetry.common.StringValuedKey;
import io.opentelemetry.context.Scope;
import org.junit.Test;

public class TracerExTest {

  private static final LongValuedKey NUMBER_ATTRIBUTE = LongValuedKey.create("number");
  private static final StringValuedKey STRING_ATTRIBUTE = StringValuedKey.create("string");
  private static final LongValuedKey LONG_ATTRIBUTE = LongValuedKey.create("long");
  private static final BooleanValuedKey BOOLEAN_ATTRIBUTE = BooleanValuedKey.create("boolean");

  @Test
  public void testTracerExApi() {
    TracerEx testTracer = new TracerEx(OpenTelemetry.getTracerProvider().get("testTracer"));

    SpanEx testSpan =
        testTracer
            .spanBuilder("testSpan")
            .addLink(SpanContext.getInvalid(), testAttributes()) // option 1
            .addLink(SpanContext.getInvalid()) // option 2 with no arg
            .addLink(
                SpanContext.getInvalid(), // option 2 with 2 args
                Attribute.create(LONG_ATTRIBUTE, 109L),
                Attribute.create(BOOLEAN_ATTRIBUTE, true))
            .addLink(LinkEx.create(SpanContext.getInvalid(), testAttributes())) // option 3
            .startSpan();
    try (Scope ignored = testTracer.withSpan(testSpan)) {
      // do some work
    }
    testSpan.end();
  }

  private static Attributes testAttributes() {
    return MapBasedAttributes.newBuilder()
        .put(NUMBER_ATTRIBUTE, 100)
        .put(STRING_ATTRIBUTE, "hello, world")
        .build();
  }
}
