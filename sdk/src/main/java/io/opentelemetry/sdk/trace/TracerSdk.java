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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.resource.Resource;
import io.opentelemetry.sdk.trace.internal.CurrentSpanUtils;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.Tracer;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;

public class TracerSdk implements Tracer {

  @Override
  public Span getCurrentSpan() {
    return CurrentSpanUtils.getCurrentSpan();
  }

  @Override
  public Scope withSpan(Span span) {
    return CurrentSpanUtils.withSpan(span);
  }

  @Override
  public Runnable withSpan(Span span, Runnable runnable) {
    return CurrentSpanUtils.withSpan(span, /* endSpan= */ false, runnable);
  }

  @Override
  public <V> Callable<V> withSpan(Span span, Callable<V> callable) {
    return CurrentSpanUtils.withSpan(span, /* endSpan= */ false, callable);
  }

  @Override
  public Span.Builder spanBuilder(String spanName) {
    return spanBuilderWithExplicitParent(spanName, getCurrentSpan());
  }

  @Override
  public Span.Builder spanBuilderWithExplicitParent(String spanName, @Nullable Span parent) {
    return null;
  }

  @Override
  public Span.Builder spanBuilderWithRemoteParent(
      String spanName, @Nullable SpanContext remoteParentSpanContext) {
    return null;
  }

  @Override
  public void setResource(Resource resource) {}

  @Override
  public Resource getResource() {
    return null;
  }

  @Override
  public void recordSpanData(SpanData span) {}

  @Override
  public BinaryFormat<SpanContext> getBinaryFormat() {
    return null;
  }

  @Override
  public HttpTextFormat<SpanContext> getHttpTextFormat() {
    return null;
  }
}
