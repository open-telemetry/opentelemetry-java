/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

/**
 * An interface for creating a lambda that is wrapped in a span and that may throw.
 *
 * @param <E> Thrown exception type.
 */
@FunctionalInterface
public interface SpanRunnable<E extends Throwable> {
  void runInSpan() throws E;
}
