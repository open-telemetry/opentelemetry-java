/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

/**
 * Convenience interface for consuming {@link Labels}.
 *
 * <p>This interface should be considered to be a FunctionalInterface in the java 8+ meaning of that
 * term.
 */
@FunctionalInterface
public interface LabelConsumer {
  void accept(String key, String value);
}
