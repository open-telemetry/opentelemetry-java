/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NoopTextMapPropagatorTest {

  @Test
  void noopFields() {
    assertThat(TextMapPropagator.noop().fields()).isEmpty();
  }
}
