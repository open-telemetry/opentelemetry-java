/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import javax.annotation.concurrent.Immutable;

/**
 * Metadata associated with an entry in baggage. This is an opaque wrapper for a String metadata
 * value.
 */
@Immutable
public interface BaggageMetadata {

  /** Returns an empty {@link BaggageMetadata}. */
  static BaggageMetadata empty() {
    return ImmutableMetadata.EMPTY;
  }

  /** Returns a new {@link BaggageMetadata} with the given value. */
  static BaggageMetadata create(String metadata) {
    return ImmutableMetadata.create(metadata);
  }

  /** Returns the String value of this {@link BaggageMetadata}. */
  String getValue();
}
