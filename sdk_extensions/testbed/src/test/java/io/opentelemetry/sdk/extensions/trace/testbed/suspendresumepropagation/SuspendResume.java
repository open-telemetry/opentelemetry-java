/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.suspendresumepropagation;

import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;

final class SuspendResume {
  private final Span span;

  public SuspendResume(int id, Tracer tracer) {
    Span span = tracer.spanBuilder("job " + id).startSpan();
    span.setAttribute("component", "suspend-resume");
    try (Scope scope = TracingContextUtils.currentContextWith(span)) {
      this.span = span;
    }
  }

  public void doPart(String name) {
    try (Scope scope = TracingContextUtils.currentContextWith(span)) {
      span.addEvent("part: " + name);
    }
  }

  public void done() {
    span.end();
  }
}
