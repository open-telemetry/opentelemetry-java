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

package io.opentelemetry.context;

import io.grpc.Context;
import javax.annotation.concurrent.Immutable;

/**
 * Util methods/functionality to interact with the {@link io.grpc.Context}.
 *
 * @since 0.1.0
 */
@Immutable
public final class ContextUtils {
  /**
   * Sets the specified {@code Context} as {@code Context.current()}, returning a {@link Scope} to
   * end its active state and restore the previous active {@code Context}.
   *
   * @param context the {@code Context} to be set as {@code Context.current()}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.1.0
   */
  public static Scope withScopedContext(Context context) {
    if (context == null) {
      throw new NullPointerException("context");
    }

    return new ContextInScope(context);
  }

  private static final class ContextInScope implements Scope {
    private final Context context;
    private final Context previous;

    public ContextInScope(Context context) {
      this.context = context;
      this.previous = context.attach();
    }

    @Override
    public void close() {
      context.detach(previous);
    }
  }

  private ContextUtils() {}
}
