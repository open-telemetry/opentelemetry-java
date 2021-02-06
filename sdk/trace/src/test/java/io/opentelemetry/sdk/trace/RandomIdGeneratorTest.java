/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.SpanIdHex;
import io.opentelemetry.api.trace.TraceIdHex;
import org.junit.jupiter.api.Test;

class RandomIdGeneratorTest {

  @Test
  void defaults() {
    IdGenerator generator = IdGenerator.random();

    // Can't assert values but can assert they're valid, try a lot as a sort of fuzz check.
    for (int i = 0; i < 1000; i++) {
      CharSequence traceId = generator.generateTraceId();
      assertThat(traceId.toString()).isNotEqualTo(TraceIdHex.getInvalid());

      CharSequence spanId = generator.generateSpanId();
      assertThat(spanId.toString()).isNotEqualTo(SpanIdHex.getInvalid());
    }
  }
}
