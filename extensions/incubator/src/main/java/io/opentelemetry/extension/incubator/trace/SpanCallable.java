/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

/**
 * An interface for creating a lambda that is wrapped in a span, returns a value, and that may
 * throw.
 *
 * @param <E> Thrown exception type.
 */
@FunctionalInterface
public interface SpanCallable<T, E extends Throwable> {
  T callInSpan() throws E;
}
