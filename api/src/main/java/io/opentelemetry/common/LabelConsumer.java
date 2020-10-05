/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

/**
 * Convenience interface for consuming {@link Labels}.
 *
 * <p>This interface should be considered to be a FunctionalInterface in the java 8+ meaning of that
 * term.
 *
 * @since 0.9.0
 */
public interface LabelConsumer {
  void consume(String key, String value);
}
