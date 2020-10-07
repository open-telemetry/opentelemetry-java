/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

/**
 * Used for iterating over all the key/value pairs in an {@link Attributes} instance.
 *
 * @since 0.9.0
 */
public interface AttributeConsumer {
  <T> void consume(AttributeKey<T> key, T value);
}
