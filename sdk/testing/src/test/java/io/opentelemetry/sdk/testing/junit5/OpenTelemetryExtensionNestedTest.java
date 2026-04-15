/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit5;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OpenTelemetryExtensionNestedTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer("test");

  @Nested
  class FirstNestedClass {

    @Test
    void recordSpan() {
      String testSpanName = "span-from-first-nested";
      tracer.spanBuilder(testSpanName).startSpan().end();

      assertThat(otelTesting.getSpans())
          .singleElement()
          .satisfies(span -> assertThat(span.getName()).isEqualTo(testSpanName));
    }
  }

  @Nested
  class SecondNestedClass {
    @Test
    void recordSpan() {
      String testSpanName = "span-from-second-nested";
      tracer.spanBuilder(testSpanName).startSpan().end();

      assertThat(otelTesting.getSpans())
          .singleElement()
          .satisfies(span -> assertThat(span.getName()).isEqualTo(testSpanName));
    }
  }
}
