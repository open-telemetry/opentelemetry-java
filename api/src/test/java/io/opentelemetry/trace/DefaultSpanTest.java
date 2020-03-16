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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.common.AttributeValue;
import java.util.Collections;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultSpan}. */
@RunWith(JUnit4.class)
public class DefaultSpanTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void hasInvalidContextAndDefaultSpanOptions() {
    SpanContext context = DefaultSpan.createRandom().getContext();
    assertThat(context.getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(context.getTraceState()).isEqualTo(TraceState.getDefault());
  }

  @Test
  public void hasUniqueTraceIdAndSpanId() {
    DefaultSpan span1 = DefaultSpan.createRandom();
    DefaultSpan span2 = DefaultSpan.createRandom();
    assertThat(span1.getContext().getTraceId()).isNotEqualTo(span2.getContext().getTraceId());
    assertThat(span1.getContext().getSpanId()).isNotEqualTo(span2.getContext().getSpanId());
  }

  @Test
  public void doNotCrash() {
    DefaultSpan span = DefaultSpan.createRandom();
    span.setAttribute(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    span.setAttribute("MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true));
    span.setAttribute("MyLongAttributeKey", AttributeValue.longAttributeValue(123));
    span.setAttribute("NullString", (String) null);
    span.setAttribute("EmptyString", "");
    span.setAttribute("NullArrayString", AttributeValue.arrayAttributeValue((String[]) null));
    span.setAttribute("NullArrayBoolean", AttributeValue.arrayAttributeValue((Boolean[]) null));
    span.setAttribute("NullArrayLong", AttributeValue.arrayAttributeValue((Long[]) null));
    span.setAttribute("NullArrayDouble", AttributeValue.arrayAttributeValue((Double[]) null));
    span.addEvent("event");
    span.addEvent("event", 0);
    span.addEvent(
        "event",
        Collections.singletonMap(
            "MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true)));
    span.addEvent(
        "event",
        Collections.singletonMap(
            "MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true)),
        0);
    span.addEvent(new TestEvent());
    span.addEvent(new TestEvent(), 0);
    span.setStatus(Status.OK);
    span.end();
    span.end(EndSpanOptions.getDefault());
  }

  @Test
  public void defaultSpan_ToString() {
    DefaultSpan span = DefaultSpan.createRandom();
    assertThat(span.toString()).isEqualTo("DefaultSpan");
  }

  @Test
  public void defaultSpan_NullEndSpanOptions() {
    DefaultSpan span = DefaultSpan.getInvalid();
    thrown.expect(NullPointerException.class);
    span.end(null);
  }

  static final class TestEvent implements Event {
    @Override
    public String getName() {
      return "name";
    }

    @Override
    public Map<String, AttributeValue> getAttributes() {
      return Collections.emptyMap();
    }
  }
}
