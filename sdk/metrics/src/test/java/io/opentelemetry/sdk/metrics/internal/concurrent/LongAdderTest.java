/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LongAdderTest {

  @Test
  void jreLongAdder() {
    validateLongAdder(new JreLongAdder());
  }

  @Test
  void atomicLongLongAdder() {
    validateLongAdder(new AtomicLongLongAdder());
  }

  void validateLongAdder(LongAdder adder) {
    adder.add(5);
    adder.add(7);
    assertThat(adder.sum()).isEqualTo(12);
    assertThat(adder.longValue()).isEqualTo(12);
    assertThat(adder.intValue()).isEqualTo(12);
    assertThat(adder.floatValue()).isEqualTo(12);
    assertThat(adder.doubleValue()).isEqualTo(12);

    assertThat(adder.sumThenReset()).isEqualTo(12);
    assertThat(adder.sum()).isEqualTo(0);

    adder.increment();
    adder.increment();
    assertThat(adder.sum()).isEqualTo(2);

    adder.decrement();
    assertThat(adder.sum()).isEqualTo(1);

    adder.reset();
    assertThat(adder.sum()).isEqualTo(0);
  }
}
