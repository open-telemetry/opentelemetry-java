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

  /**
   * Returns a new {@link BaggageEntryMetadata} with the given value.
   *
   * <p>For W3C baggage propagation, {@code metadata} must contain only tab (0x09) or printable
   * ASCII (0x20-0x7E) excluding {@code "}, {@code ,}, and {@code \}. Entries whose metadata
   * contains other characters are dropped on inject.
   */
  static BaggageEntryMetadata create(String metadata) {
    return ImmutableEntryMetadata.create(metadata);
  }

  /** Returns the String value of this {@link BaggageEntryMetadata}. */
  String getValue();
}
