/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

/** A consumer of entries in {@link Baggage}. */
@FunctionalInterface
public interface BaggageConsumer {
  /** Consumes an entry of a {@link Baggage}. */
  void accept(String key, String value, BaggageMetadata metadata);
}
