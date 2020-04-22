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

import io.opentelemetry.trace.Span.Kind;

public class SpanEx {

  private final Span span;

  public SpanEx(Span span) {
    this.span = span;
  }

  Span getSpan() {
    return span;
  }

  public void setStatus(Status status) {
    span.setStatus(status);
  }

  public void updateName(String name) {
    span.updateName(name);
  }

  public SpanContext getContext() {
    return span.getContext();
  }

  public boolean isRecording() {
    return span.isRecording();
  }

  public void end() {
    span.end();
  }

  public void end(EndSpanOptions endOptions) {
    span.end(endOptions);
  }

  public static Builder newBuilder(Tracer tracer, String spanName) {
    return new Builder(tracer, spanName);
  }

  public static class Builder {

    private final Span.Builder builder;

    public Builder(Tracer tracer, String spanName) {
      builder = tracer.spanBuilder(spanName);
    }

    public Span.Builder setParent(SpanEx parent) {
      return builder.setParent(parent.span);
    }

    public Span.Builder setParent(SpanContext remoteParent) {
      return builder.setParent(remoteParent);
    }

    public Span.Builder setNoParent() {
      return builder.setNoParent();
    }

    public Span.Builder setSpanKind(Kind spanKind) {
      return builder.setSpanKind(spanKind);
    }

    public Span.Builder setStartTimestamp(long startTimestamp) {
      return builder.setStartTimestamp(startTimestamp);
    }

    public SpanEx startSpan() {
      return new SpanEx(builder.startSpan());
    }
  }
}
