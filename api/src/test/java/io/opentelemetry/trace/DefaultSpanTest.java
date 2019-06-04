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

/** Unit tests for {@link DefaultSpan}. */
@RunWith(JUnit4.class)
public class DefaultSpanTest {
  @Test
  public void hasInvalidContextAndDefaultSpanOptions() {
    assertThat(DefaultSpan.INSTANCE.getContext()).isEqualTo(SpanContext.BLANK);
  }

  @Test
  public void doNotCrash() {
    DefaultSpan.INSTANCE.setAttribute(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    DefaultSpan.INSTANCE.setAttribute(
        "MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true));
    DefaultSpan.INSTANCE.setAttribute("MyLongAttributeKey", AttributeValue.longAttributeValue(123));
    DefaultSpan.INSTANCE.addEvent("event");
    DefaultSpan.INSTANCE.addEvent(
        "event",
        Collections.singletonMap(
            "MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true)));
    DefaultSpan.INSTANCE.addEvent(SpanData.Event.create("event"));
    DefaultSpan.INSTANCE.addLink(Link.create(SpanContext.BLANK));
    DefaultSpan.INSTANCE.setStatus(Status.OK);
    DefaultSpan.INSTANCE.end();
  }

  @Test
  public void blankSpan_ToString() {
    assertThat(DefaultSpan.INSTANCE.toString()).isEqualTo("DefaultSpan");
  }
}
