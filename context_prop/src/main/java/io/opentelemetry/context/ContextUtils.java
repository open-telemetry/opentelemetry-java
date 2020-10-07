/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
