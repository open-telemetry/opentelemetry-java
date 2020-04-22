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

public class SpanEx {

  private final Span span;

  public SpanEx(Span span) {
    this.span = span;
  }

  public static Builder newBuilder(Tracer tracer, String spanName) {
    return new Builder(tracer, spanName);
  }

  Span getSpan() {
    return span;
  }

  public void end() {
    span.end();
  }

  public void end(EndSpanOptions endOptions) {
    span.end(endOptions);
  }

  public static class Builder {

    private final Span.Builder builder;

    public Builder(Tracer tracer, String spanName) {
      builder = tracer.spanBuilder(spanName);
    }

    public SpanEx startSpan() {
      return new SpanEx(builder.startSpan());
    }
  }
}
