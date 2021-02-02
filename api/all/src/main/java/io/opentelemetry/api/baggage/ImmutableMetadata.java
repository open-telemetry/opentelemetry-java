/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class ImmutableMetadata implements BaggageMetadata {
  /** Returns an empty metadata. */
  static final ImmutableMetadata EMPTY = create("");

  ImmutableMetadata() {}

  /**
   * Creates an {@link ImmutableMetadata} with the given value.
   *
   * @param metadata TTL of an {@code Entry}.
   * @return an {@code EntryMetadata}.
   */
  static ImmutableMetadata create(String metadata) {
    if (metadata == null) {
      return EMPTY;
    }
    return new AutoValue_ImmutableMetadata(metadata);
  }

  @Override
  public abstract String getValue();
}
