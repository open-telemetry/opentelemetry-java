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

package io.opentelemetry.trace;

import static io.opentelemetry.common.AttributesKeys.booleanArrayKey;
import static io.opentelemetry.common.AttributesKeys.booleanKey;
import static io.opentelemetry.common.AttributesKeys.doubleArrayKey;
import static io.opentelemetry.common.AttributesKeys.longArrayKey;
import static io.opentelemetry.common.AttributesKeys.longKey;
import static io.opentelemetry.common.AttributesKeys.stringArrayKey;
import static io.opentelemetry.common.AttributesKeys.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Attributes;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultSpan}. */
class DefaultSpanTest {

  @Test
  void hasInvalidContextAndDefaultSpanOptions() {
    SpanContext context = DefaultSpan.getInvalid().getContext();
    assertThat(context.getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(context.getTraceState()).isEqualTo(TraceState.getDefault());
  }

  @Test
  void doNotCrash() {
    Span span = DefaultSpan.getInvalid();
    span.setAttribute(stringKey("MyStringAttributeKey"), "MyStringAttributeValue");
    span.setAttribute(booleanKey("MyBooleanAttributeKey"), true);
    span.setAttribute(longKey("MyLongAttributeKey"), 123L);
    span.setAttribute(longKey("MyLongAttributeKey"), 123);
    span.setAttribute("NullString", null);
    span.setAttribute("EmptyString", "");
    span.setAttribute(stringArrayKey("NullArrayString"), null);
    span.setAttribute(booleanArrayKey("NullArrayBoolean"), null);
    span.setAttribute(longArrayKey("NullArrayLong"), null);
    span.setAttribute(doubleArrayKey("NullArrayDouble"), null);
    span.setAttribute((String) null, null);
    span.addEvent("event");
    span.addEvent("event", 0);
    span.addEvent("event", Attributes.of(booleanKey("MyBooleanAttributeKey"), true));
    span.addEvent("event", Attributes.of(booleanKey("MyBooleanAttributeKey"), true), 0);
    span.setStatus(Status.OK);
    span.recordException(new IllegalStateException());
    span.recordException(new IllegalStateException(), Attributes.empty());
    span.end();
    span.end(EndSpanOptions.getDefault());
    span.end(null);
  }

  @Test
  void defaultSpan_ToString() {
    Span span = DefaultSpan.getInvalid();
    assertThat(span.toString()).isEqualTo("DefaultSpan");
  }
}
