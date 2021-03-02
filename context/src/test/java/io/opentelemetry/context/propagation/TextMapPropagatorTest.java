/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TextMapPropagatorTest {

  @Mock private TextMapPropagator propagator;

  @Test
  void compositeNonMulti() {
    assertThat(TextMapPropagator.composite()).isSameAs(TextMapPropagator.noop());
    assertThat(TextMapPropagator.composite(propagator)).isSameAs(propagator);
  }
}
