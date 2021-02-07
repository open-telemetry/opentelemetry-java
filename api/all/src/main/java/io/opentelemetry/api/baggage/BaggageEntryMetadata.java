/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

/**
 * Metadata associated with an entry in baggage. This is an opaque wrapper for a String metadata
 * value.
 */
public interface BaggageEntryMetadata {

  /** Returns an empty {@link BaggageEntryMetadata}. */
  static BaggageEntryMetadata empty() {
    return EntryMetadata.EMPTY;
  }

  /** Returns a new {@link BaggageEntryMetadata} with the given value. */
  static BaggageEntryMetadata create(String metadata) {
    return EntryMetadata.create(metadata);
  }

  /** Returns the String value of this {@link BaggageEntryMetadata}. */
  String getValue();
}
