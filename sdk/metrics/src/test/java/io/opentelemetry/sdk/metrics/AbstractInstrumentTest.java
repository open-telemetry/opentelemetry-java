/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AbstractInstrument}. */
class AbstractInstrumentTest {
  private static final InstrumentDescriptor INSTRUMENT_DESCRIPTOR =
      InstrumentDescriptor.create(
          "name",
          "description",
          "1",
          InstrumentType.COUNTER,
          InstrumentValueType.LONG,
          Advice.empty());

  @Test
  void getValues() {
    TestInstrument testInstrument = new TestInstrument(INSTRUMENT_DESCRIPTOR);
    assertThat(testInstrument.getDescriptor()).isSameAs(INSTRUMENT_DESCRIPTOR);
  }

  @Test
  void stringRepresentation() {
    TestInstrument testInstrument = new TestInstrument(INSTRUMENT_DESCRIPTOR);
    assertThat(testInstrument)
        .hasToString("TestInstrument{descriptor=" + INSTRUMENT_DESCRIPTOR + "}");
  }

  private static final class TestInstrument extends AbstractInstrument {
    TestInstrument(InstrumentDescriptor descriptor) {
      super(descriptor);
    }
  }
}
