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
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TimedEvent}. */
@RunWith(JUnit4.class)
public class TimedEventTest {

  private static final String NAME = "event";
  private static final String NAME_2 = "event2";
  private static final Map<String, AttributeValue> ATTRIBUTES =
      Collections.singletonMap("attribute", AttributeValue.stringAttributeValue("value"));
  private static final Map<String, AttributeValue> ATTRIBUTES_2 =
      Collections.singletonMap("attribute2", AttributeValue.stringAttributeValue("value2"));
  private static final Event EVENT =
      new Event() {
        @Override
        public String getName() {
          return NAME_2;
        }

        @Override
        public Map<String, AttributeValue> getAttributes() {
          return ATTRIBUTES_2;
        }
      };

  @Test
  public void rawTimedEventWithName() {
    TimedEvent event = TimedEvent.create(1000, NAME);
    assertThat(event.getNanotime()).isEqualTo(1000);
    assertThat(event.getName()).isEqualTo(NAME);
    assertThat(event.getAttributes()).isEmpty();
  }

  @Test
  public void rawTimedEventWithNameAndAttributes() {
    TimedEvent event = TimedEvent.create(2000, NAME, ATTRIBUTES);
    assertThat(event.getNanotime()).isEqualTo(2000);
    assertThat(event.getName()).isEqualTo(NAME);
    assertThat(event.getAttributes()).isEqualTo(ATTRIBUTES);
  }

  @Test
  public void timedEventWithEvent() {
    TimedEvent event = TimedEvent.create(3000, EVENT);
    assertThat(event.getNanotime()).isEqualTo(3000);
    assertThat(event.getName()).isEqualTo(NAME_2);
    assertThat(event.getAttributes()).isEqualTo(ATTRIBUTES_2);
  }
}
