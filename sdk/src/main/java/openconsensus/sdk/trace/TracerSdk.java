/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.sdk.trace;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import openconsensus.context.Scope;
import openconsensus.context.propagation.BinaryFormat;
import openconsensus.context.propagation.HttpTextFormat;
import openconsensus.resource.Resource;
import openconsensus.sdk.trace.internal.CurrentSpanUtils;
import openconsensus.trace.Span;
import openconsensus.trace.SpanContext;
import openconsensus.trace.SpanData;
import openconsensus.trace.Tracer;

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
