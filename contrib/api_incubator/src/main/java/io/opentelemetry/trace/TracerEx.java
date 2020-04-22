/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.trace;

import com.google.errorprone.annotations.MustBeClosed;
import io.opentelemetry.context.Scope;

// go Speed Racer, go Speed Racer, go Speed Racer go!
public class TracerEx {

  private final Tracer tracer;

  public TracerEx(Tracer tracer) {
    this.tracer = tracer;
  }

  public SpanEx getCurrentSpan() {
    return new SpanEx(tracer.getCurrentSpan());
  }

  @MustBeClosed
  public Scope withSpan(SpanEx span) {
    return tracer.withSpan(span.getSpan());
  }

  public SpanEx.Builder spanBuilder(String spanName) {
    return SpanEx.newBuilder(tracer, spanName);
  }
}
