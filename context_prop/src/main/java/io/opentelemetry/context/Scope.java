/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.io.Closeable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A {@link java.io.Closeable} that represents a change to the current context over a scope of code.
 * {@link Scope#close} cannot throw a checked exception.
 *
 * <p>Example of usage:
 *
 * <pre>
 *   try (Scope ctx = tracer.withSpan(span)) {
 *     ...
 *   }
 * </pre>
 *
 * @since 0.1.0
 */
@NotThreadSafe
public interface Scope extends Closeable {
  @Override
  void close();
}
