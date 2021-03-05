/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import javax.annotation.concurrent.Immutable;

/**
 * Metadata associated with an {@link BaggageEntry}. For the moment this is an opaque wrapper for a
 * String metadata value.
 */
@Immutable
public interface BaggageEntryMetadata {

  /** Returns an empty {@link BaggageEntryMetadata}. */
  static BaggageEntryMetadata empty() {
    return ImmutableEntryMetadata.EMPTY;
  }

  /** Returns a new {@link BaggageEntryMetadata} with the given value. */
  static BaggageEntryMetadata create(String metadata) {
    return ImmutableEntryMetadata.create(metadata);
  }

  /** Returns the String value of this {@link BaggageEntryMetadata}. */
  String getValue();
}
