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

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link BlankSpan}. */
@RunWith(JUnit4.class)
public class BlankSpanTest {
  @Test
  public void hasInvalidContextAndDefaultSpanOptions() {
    assertThat(BlankSpan.INSTANCE.getContext()).isEqualTo(SpanContext.BLANK);
  }

  @Test
  public void doNotCrash() {
    BlankSpan.INSTANCE.setAttribute(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    BlankSpan.INSTANCE.setAttribute(
        "MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true));
    BlankSpan.INSTANCE.setAttribute("MyLongAttributeKey", AttributeValue.longAttributeValue(123));
    BlankSpan.INSTANCE.addEvent("event");
    BlankSpan.INSTANCE.addEvent(
        "event",
        Collections.singletonMap(
            "MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true)));
    BlankSpan.INSTANCE.addEvent(SpanData.Event.create("event"));
    BlankSpan.INSTANCE.addLink(Link.create(SpanContext.BLANK));
    BlankSpan.INSTANCE.setStatus(Status.OK);
    BlankSpan.INSTANCE.end();
  }

  @Test
  public void blankSpan_ToString() {
    assertThat(BlankSpan.INSTANCE.toString()).isEqualTo("BlankSpan");
  }
}
