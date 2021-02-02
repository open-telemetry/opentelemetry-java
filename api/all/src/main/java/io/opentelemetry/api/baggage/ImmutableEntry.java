/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class ImmutableEntry implements BaggageEntry {

  ImmutableEntry() {}

  /**
   * Creates an {@code Entry} from the given key, value and metadata.
   *
   * @param value the entry value.
   * @param entryMetadata the entry metadata.
   * @return a {@code Entry}.
   */
  static ImmutableEntry create(String value, BaggageMetadata entryMetadata) {
    return new AutoValue_ImmutableEntry(value, entryMetadata);
  }
}
