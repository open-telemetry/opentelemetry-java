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

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.trace.Event;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TimedEvent}. */
class TimedEventTest {

  private static final String NAME = "event";
  private static final String NAME_2 = "event2";
  private static final Attributes ATTRIBUTES =
      Attributes.of("attribute", AttributeValue.stringAttributeValue("value"));
  private static final Attributes ATTRIBUTES_2 =
      Attributes.of("attribute2", AttributeValue.stringAttributeValue("value2"));
  private static final Event EVENT =
      new Event() {
        @Override
        public String getName() {
          return NAME_2;
        }

        @Override
        public Attributes getAttributes() {
          return ATTRIBUTES_2;
        }
      };

  @Test
  void rawTimedEventWithNameAndAttributesAndTotalAttributeCount() {
    TimedEvent event = TimedEvent.create(1234567890L, NAME, ATTRIBUTES, ATTRIBUTES.size() + 2);
    assertThat(event.getEpochNanos()).isEqualTo(1234567890L);
    assertThat(event.getName()).isEqualTo(NAME);
    assertThat(event.getAttributes()).isEqualTo(ATTRIBUTES);
    assertThat(event.getTotalAttributeCount()).isEqualTo(ATTRIBUTES.size() + 2);
  }

  @Test
  void rawTimedEventWithEvent() {
    TimedEvent event = TimedEvent.create(9876501234L, EVENT);
    assertThat(event.getEpochNanos()).isEqualTo(9876501234L);
    assertThat(event.getName()).isEqualTo(NAME_2);
    assertThat(event.getAttributes()).isEqualTo(ATTRIBUTES_2);
  }
}
