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

package io.opentelemetry.opentracingshim;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

final class ScopeManagerShim extends BaseShimObject implements ScopeManager {

  public ScopeManagerShim(TelemetryInfo telemetryInfo) {
    super(telemetryInfo);
  }

  @Override
  @SuppressWarnings("ReturnMissingNullable")
  public Span activeSpan() {
    // As OpenTracing simply returns null when no active instance is available,
    // we need to do an explicit check against DefaultSpan,
    // which is used in OpenTelemetry for this very case.
    io.opentelemetry.trace.Span span = tracer().getCurrentSpan();
    if (io.opentelemetry.trace.DefaultSpan.getInvalid().equals(span)) {
      return null;
    }

    // TODO: Properly include the bagagge/distributedContext.
    return new SpanShim(telemetryInfo(), span);
  }

  @Override
  @SuppressWarnings("MustBeClosedChecker")
  public Scope activate(Span span) {
    io.opentelemetry.trace.Span actualSpan = getActualSpan(span);
    return new ScopeShim(tracer().withSpan(actualSpan));
  }

  static io.opentelemetry.trace.Span getActualSpan(Span span) {
    if (!(span instanceof SpanShim)) {
      throw new IllegalArgumentException("span is not a valid SpanShim object");
    }

    return ((SpanShim) span).getSpan();
  }
}
