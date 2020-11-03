/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.suspendresumepropagation;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

final class SuspendResume {
  private final Tracer tracer;
  private final Span span;

  public SuspendResume(int id, Tracer tracer) {
    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;

    Span span =
        tracer.buildSpan("job " + id).withTag(Tags.COMPONENT.getKey(), "suspend-resume").start();
    try (Scope scope = tracer.scopeManager().activate(span)) {
      this.span = span;
    }
  }

  public void doPart(String name) {
    try (Scope scope = tracer.scopeManager().activate(span)) {
      span.log("part: " + name);
    }
  }

  public void done() {
    span.finish();
  }
}
