/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import javax.annotation.concurrent.Immutable;

/**
 * {@link EntryMetadata} contains properties associated with an {@link Entry}. This is an opaque
 * wrapper for a String metadata value.
 */
@Immutable
public final class EntryMetadata {
  private static final EntryMetadata EMPTY = create("");

  /** Returns an {@link EntryMetadata} with no value. */
  public static EntryMetadata empty() {
    return EMPTY;
  }

  /** Returns an {@link EntryMetadata} with the given value. */
  public static EntryMetadata create(String metadata) {
    if (metadata == null) {
      return empty();
    }
    return new EntryMetadata(metadata);
  }

  private final String metadata;

  private EntryMetadata(String metadata) {
    this.metadata = metadata;
  }

  /**
   * Returns the String value of this {@link EntryMetadata}.
   *
   * @return the raw metadata value.
   */
  public String getValue() {
    return metadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EntryMetadata)) {
      return false;
    }
    EntryMetadata that = (EntryMetadata) o;
    return metadata.equals(that.metadata);
  }

  @Override
  public int hashCode() {
    return metadata.hashCode();
  }

  @Override
  public String toString() {
    return "'" + metadata + "'";
  }
}
