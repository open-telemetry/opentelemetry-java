/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.contrib.trace.testbed.suspendresumepropagation;

import io.opentelemetry.currentcontext.CurrentContext;
import io.opentelemetry.currentcontext.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

final class SuspendResume {
  private final Span span;

  public SuspendResume(int id, Tracer tracer) {
    // Tracer passed along here for testing. Normally should be referenced via GlobalTracer.get().

    Span span = tracer.spanBuilder("job " + id).startSpan();
    span.setAttribute("component", "suspend-resume");
    try (Scope scope = CurrentContext.withSpan(span)) {
      this.span = span;
    }
  }

  public void doPart(String name) {
    try (Scope scope = CurrentContext.withSpan(span)) {
      span.addEvent("part: " + name);
    }
  }

  public void done() {
    span.end();
  }
}
