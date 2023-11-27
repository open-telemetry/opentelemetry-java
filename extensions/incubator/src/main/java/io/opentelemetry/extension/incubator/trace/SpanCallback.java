/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

/**
 * An interface for creating a lambda that is wrapped in a span, returns a value, and that may
 * throw, similar to <a
 * href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionCallback.html">TransactionCallback</a>.
 *
 * @param <E> Thrown exception type.
 */
@FunctionalInterface
public interface SpanCallback<T, E extends Throwable> {
  T doInSpan() throws E;
}
