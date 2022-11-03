/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Tracing;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

// todo remove if/once the underlying issue is agreed upon
public class IntegrationTest {

  @Test
  void test() {
    Tracer tracer = GlobalOpenTelemetry.get().getTracer("test");
    Span span = tracer.spanBuilder("test-span").setSpanKind(SpanKind.INTERNAL).startSpan();
    Scope scope = span.makeCurrent();
    try {
      io.opencensus.trace.Tracer ocTracer = Tracing.getTracer();
      io.opencensus.trace.Span internal = ocTracer.spanBuilder("internal").startSpan();
      io.opencensus.common.Scope ocScope = ocTracer.withSpan(internal);
      try {
        ocTracer
            .getCurrentSpan()
            .putAttribute("internal-only", AttributeValue.booleanAttributeValue(true));
      } finally {
        ocScope.close();
      }
      internal.end();
    } finally {
      scope.close();
    }
    span.end();
  }
}
