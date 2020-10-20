/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import io.opentelemetry.context.ThreadLocalContextStorage.NoopScope;

/**
 * An {@link AutoCloseable} that represents a mounted context for a block of code. A failure to call
 * {@link Scope#close()} will generally break tracing or cause memory leaks. It is recommended that
 * you use this class with a {@code try-with-resources} block:
 *
 * <pre>{@code
 * try (Scope ignored = TracingContextUtils.currentContextWith(span)) {
 *   ...
 * }
 * }</pre>
 */
public interface Scope extends AutoCloseable {

  /**
   * Returns a {@link Scope} that does nothing. Represents attaching a {@link Context} when it is
   * already attached.
   */
  static Scope noop() {
    return NoopScope.INSTANCE;
  }

  @Override
  void close();
}
