/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/** String-String key-value pair, along with {@link EntryMetadata}. */
@Immutable
@AutoValue
abstract class Entry implements BaggageEntry {

  Entry() {}

  /**
   * Creates an {@code Entry} from the given key, value and metadata.
   *
   * @param value the entry value.
   * @param entryMetadata the entry metadata.
   * @return a {@code Entry}.
   */
  static Entry create(String value, BaggageEntryMetadata entryMetadata) {
    return new AutoValue_Entry(value, entryMetadata);
  }
}
