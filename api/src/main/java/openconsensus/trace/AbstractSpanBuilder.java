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

import java.util.List;
import java.util.concurrent.Callable;

/**
 * An abstract class that implements {@code SpanBuilder}.
 *
 * <p>Users are encouraged to extend this class for convenience.
 *
 * @since 0.1.0
 */
public abstract class AbstractSpanBuilder implements SpanBuilder {
  @Override
  public abstract SpanBuilder setSampler(Sampler sampler);

  @Override
  public abstract SpanBuilder setParentLinks(List<Span> parentLinks);

  @Override
  public abstract SpanBuilder setRecordEvents(boolean recordEvents);

  @Override
  public SpanBuilder setSpanKind(Span.Kind spanKind) {
    return this;
  }

  @Override
  public abstract Span startSpan();

  @Override
  public abstract void startSpanAndRun(final Runnable runnable);

  @Override
  public abstract <V> V startSpanAndCall(Callable<V> callable) throws Exception;
}
