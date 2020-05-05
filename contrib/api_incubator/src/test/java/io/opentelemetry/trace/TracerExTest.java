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

import static io.opentelemetry.common.AttributeKey.booleanKey;
import static io.opentelemetry.common.AttributeKey.doubleKey;
import static io.opentelemetry.common.AttributeKey.longKey;
import static io.opentelemetry.common.AttributeKey.stringKey;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.ArrayBasedAttributes;
import io.opentelemetry.common.Attribute;
import io.opentelemetry.common.AttributeKey.BooleanValuedKey;
import io.opentelemetry.common.AttributeKey.DoubleValuedKey;
import io.opentelemetry.common.AttributeKey.LongValuedKey;
import io.opentelemetry.common.AttributeKey.StringValuedKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.SemanticAttributes;
import io.opentelemetry.context.Scope;
import org.junit.Test;

public class TracerExTest {

  private static final DoubleValuedKey DOUBLE_ATTRIBUTE = doubleKey("number");
  private static final StringValuedKey STRING_ATTRIBUTE = stringKey("string");
  private static final LongValuedKey LONG_ATTRIBUTE = longKey("long");
  private static final BooleanValuedKey BOOLEAN_ATTRIBUTE = booleanKey("boolean");

  @Test
  public void testTracerExApi_linkOptions() {
    TracerEx testTracer = new TracerEx(OpenTelemetry.getTracerProvider().get("testTracer"));

    SpanEx testSpan =
        testTracer
            .spanBuilder("testSpan")
            .addLink(SpanContext.getInvalid(), testAttributes()) // option 1
            .addLink(SpanContext.getInvalid()) // option 2 with no arg
            // option 2 with 2 args:
            .addLink(
                SpanContext.getInvalid(),
                Attribute.create(LONG_ATTRIBUTE, 109L),
                Attribute.create(BOOLEAN_ATTRIBUTE, true))
            .addLink(LinkEx.create(SpanContext.getInvalid(), testAttributes())) // option 3
            .startSpan();
    try (Scope ignored = testTracer.withSpan(testSpan)) {
      // do some work
    }
    testSpan.end();
  }

  @Test
  public void testTracerExApi_setAttributes() {
    TracerEx testTracer = new TracerEx(OpenTelemetry.getTracerProvider().get("testTracer"));

    SpanEx testSpan =
        testTracer
            .spanBuilder("testSpan")
            // option 1 - single
            .setAttribute(Attribute.create(SemanticAttributes.HTTP_METHOD, "GET"))
            // option 2 - multiple
            .setAttribute(
                Attribute.create(SemanticAttributes.HTTP_METHOD, "GET"),
                Attribute.create(SemanticAttributes.NET_HOST_PORT, 8080))
            // option 3 - bunch
            .setAttribute(httpAttribute())
            .startSpan();
    try (Scope ignored = testTracer.withSpan(testSpan)) {
      // do some work
    }
    testSpan.end();
  }

  private static Attributes testAttributes() {
    return ArrayBasedAttributes.newBuilder()
        .put(DOUBLE_ATTRIBUTE, 100.777635353d)
        .put(STRING_ATTRIBUTE, "hello, world")
        .build();
  }

  private static Attributes httpAttribute() {
    return ArrayBasedAttributes.newBuilder()
        .put(SemanticAttributes.HTTP_METHOD, "GET")
        .put(SemanticAttributes.HTTP_TARGET, "/my/example")
        .put(SemanticAttributes.HTTP_HOST, "opentelemetry.io")
        .put(SemanticAttributes.HTTP_SCHEME, "https")
        .put(SemanticAttributes.NET_PEER_IP, "192.168.0.0")
        .put(SemanticAttributes.NET_PEER_PORT, 3000)
        .put(SemanticAttributes.NET_HOST_IP, "127.0.0.1")
        .put(SemanticAttributes.NET_HOST_PORT, 8080)
        .build();
  }
}
