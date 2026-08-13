/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.impl.ApiUsageLogger;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class ImmutableEntryMetadata implements BaggageEntryMetadata {
  /** Returns an empty metadata. */
  static final ImmutableEntryMetadata EMPTY = create("");

  ImmutableEntryMetadata() {}

  /**
   * Creates an {@link ImmutableEntryMetadata} with the given value.
   *
   * @param metadata TTL of an {@code Entry}.
   * @return an {@code EntryMetadata}.
   */
  static ImmutableEntryMetadata create(String metadata) {
    if (metadata == null) {
      ApiUsageLogger.logNullParam(BaggageEntryMetadata.class, "create", "metadata");
      return EMPTY;
    }
    return new AutoValue_ImmutableEntryMetadata(metadata);
  }

  /**
   * Returns the String value of this {@link ImmutableEntryMetadata}.
   *
   * @return the raw metadata value.
   */
  @Override
  public abstract String getValue();
}
