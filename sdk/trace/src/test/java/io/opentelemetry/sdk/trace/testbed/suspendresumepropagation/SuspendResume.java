/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.suspendresumepropagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

final class SuspendResume {
  private final Span span;

  public SuspendResume(int id, Tracer tracer) {
    Span span = tracer.spanBuilder("job " + id).startSpan();
    span.setAttribute("component", "suspend-resume");
    try (Scope scope = span.makeCurrent()) {
      this.span = span;
    }
  }

  public void doPart(String name) {
    try (Scope scope = span.makeCurrent()) {
      span.addEvent("part: " + name);
    }
  }

  public void done() {
    span.end();
  }
}
