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

package io.opentelemetry.trace.unsafe;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A scope that manages the {@link Context} for a {@link Span}.
 *
 * @since 0.1.0
 */
@NotThreadSafe
final class SpanInScope implements Scope {
  private final Context previous;
  private final Context current;

  private SpanInScope(Span span) {
    current = ContextUtils.withValue(span);
    previous = current.attach();
  }

  /**
   * Constructs a new {@link SpanInScope}.
   *
   * @param span the {@code Span} to be added to the current {@code Context}.
   * @since 0.1.0
   */
  static SpanInScope create(Span span) {
    return new SpanInScope(span);
  }

  @Override
  public void close() {
    current.detach(previous);
  }
}
