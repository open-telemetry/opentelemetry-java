/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

import org.junit.jupiter.api.Test;

class DoubleAdderTest {

  @Test
  void jreLongAdder() {
    validateLongAdder(new JreDoubleAdder());
  }

  @Test
  void atomicLongLongAdder() {
    validateLongAdder(new AtomicLongDoubleAdder());
  }

  void validateLongAdder(DoubleAdder adder) {
    adder.add(5.2);
    adder.add(7.4);
    assertThat(adder.sum()).isEqualTo(12.6, withPrecision(0.01));
    assertThat(adder.longValue()).isEqualTo(12);
    assertThat(adder.intValue()).isEqualTo(12);
    assertThat(adder.floatValue()).isEqualTo(12.6f);
    assertThat(adder.doubleValue()).isEqualTo(12.6, withPrecision(0.01));

    assertThat(adder.sumThenReset()).isEqualTo(12.6, withPrecision(0.01));
    assertThat(adder.sum()).isEqualTo(0);

    adder.add(5.0);
    adder.reset();
    assertThat(adder.sum()).isEqualTo(0);
  }
}
