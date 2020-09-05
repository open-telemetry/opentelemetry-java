/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
