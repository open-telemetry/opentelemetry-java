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

package openconsensus.trace;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import openconsensus.context.Scope;
import openconsensus.trace.data.SpanData;

/**
 * An abstract class that implements {@code Tracer}.
 *
 * <p>Users are encouraged to extend this class for convenience.
 *
 * @since 0.1.0
 */
public abstract class AbstractTracer implements Tracer {
  @Override
  public abstract Span getCurrentSpan();

  @Override
  public abstract Scope withSpan(Span span);

  @Override
  public abstract Runnable withSpan(Span span, Runnable runnable);

  @Override
  public abstract <V> Callable<V> withSpan(Span span, final Callable<V> callable);

  @Override
  public abstract SpanBuilder spanBuilder(String spanName);

  @Override
  public abstract SpanBuilder spanBuilderWithExplicitParent(String spanName, @Nullable Span parent);

  @Override
  public abstract SpanBuilder spanBuilderWithRemoteParent(
      String spanName, @Nullable SpanContext remoteParentSpanContext);

  @Override
  public abstract void recordSpanData(SpanData span);

  protected AbstractTracer() {}
}
