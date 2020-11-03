/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

/** Used for iterating over all the key/value pairs in an {@link Attributes} instance. */
@FunctionalInterface
public interface AttributeConsumer {
  <T> void accept(AttributeKey<T> key, T value);
}
