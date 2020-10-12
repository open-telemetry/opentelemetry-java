/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * {@link EntryMetadata} contains properties associated with an {@link Entry}. This is an opaque
 * wrapper for a String metadata value.
 *
 * @since 0.9.0
 */
@Immutable
@AutoValue
public abstract class EntryMetadata {
  public static final EntryMetadata EMPTY = create("");

  EntryMetadata() {}

  /**
   * Creates an {@link EntryMetadata} with the given value.
   *
   * @param metadata TTL of an {@code Entry}.
   * @return an {@code EntryMetadata}.
   * @since 0.9.0
   */
  public static EntryMetadata create(String metadata) {
    return new AutoValue_EntryMetadata(metadata);
  }

  /**
   * Returns the String value of this {@link EntryMetadata}.
   *
   * @return the raw metadata value.
   * @since 0.9.0
   */
  public abstract String getValue();
}
