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

import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link NoRecordEventsSpanImpl}. */
@RunWith(JUnit4.class)
public class NoRecordEventsSpanImplTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final SpanContext spanContext =
      SpanContext.create(
          TestUtils.generateRandomTraceId(),
          TestUtils.generateRandomSpanId(),
          TraceOptions.DEFAULT,
          Tracestate.builder().build());
  private static final SpanContext BLANK =
      SpanContext.create(
          TraceId.INVALID, SpanId.INVALID, TraceOptions.DEFAULT, Tracestate.builder().build());
  private final NoRecordEventsSpanImpl noRecordEventsSpan =
      NoRecordEventsSpanImpl.create(spanContext);

  @Test
  public void propagatesSpanContext() {
    assertThat(noRecordEventsSpan.getContext()).isEqualTo(spanContext);
  }

  @Test
  public void notRecordingEvents() {
    assertThat(noRecordEventsSpan.isRecordingEvents()).isFalse();
  }

  @Test
  public void disallowNullAttributeKey() {
    thrown.expect(NullPointerException.class);
    noRecordEventsSpan.setAttribute(null, 0);
  }

  @Test
  public void disallowNullAttributeValue() {
    thrown.expect(NullPointerException.class);
    noRecordEventsSpan.setAttribute("key", (AttributeValue) null);
  }

  @Test
  public void disallowNullEvent() {
    thrown.expect(NullPointerException.class);
    noRecordEventsSpan.addEvent((Event) null);
  }

  @Test
  public void disallowNullEventName() {
    thrown.expect(NullPointerException.class);
    noRecordEventsSpan.addEvent(null, Collections.<String, AttributeValue>emptyMap());
  }

  @Test
  public void disallowNullEventAttributes() {
    thrown.expect(NullPointerException.class);
    noRecordEventsSpan.addEvent("name", null);
  }

  @Test
  public void disallowNullLink() {
    thrown.expect(NullPointerException.class);
    noRecordEventsSpan.addLink(null);
  }

  @Test
  public void disallowNullName() {
    thrown.expect(NullPointerException.class);
    noRecordEventsSpan.updateName(null);
  }

  @Test
  public void disallowNullStatus() {
    thrown.expect(NullPointerException.class);
    noRecordEventsSpan.setStatus(null);
  }

  @Test
  public void doNotCrash() {
    // Tests only that all the methods are not crashing/throwing errors.
    noRecordEventsSpan.setAttribute("MyStringAttributeKey", "MyStringAttributeValue");
    noRecordEventsSpan.setAttribute("MyBooleanAttributeKey", true);
    noRecordEventsSpan.setAttribute("MyLongAttributeKey", 123);
    noRecordEventsSpan.setAttribute(
        "MyStringAttributeKey2", AttributeValue.stringAttributeValue("MyStringAttributeValue2"));
    noRecordEventsSpan.addEvent("event1");
    noRecordEventsSpan.addEvent("event2", Collections.<String, AttributeValue>emptyMap());
    noRecordEventsSpan.addLink(Link.create(BLANK));
    noRecordEventsSpan.updateName("name");
    noRecordEventsSpan.setStatus(Status.OK);
    noRecordEventsSpan.end();
  }
}
