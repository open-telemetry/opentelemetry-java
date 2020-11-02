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
public abstract class Entry {

  Entry() {}

  /**
   * Creates an {@code Entry} from the given key, value and metadata.
   *
   * @param key the entry key.
   * @param value the entry value.
   * @param entryMetadata the entry metadata.
   * @return a {@code Entry}.
   */
  static Entry create(String key, String value, EntryMetadata entryMetadata) {
    return new AutoValue_Entry(key, value, entryMetadata);
  }

  /**
   * Creates an {@code Entry} from the given key, value, with no metadata.
   *
   * @param key the entry key.
   * @param value the entry value.
   * @return a {@code Entry}.
   */
  static Entry create(String key, String value) {
    return create(key, value, EntryMetadata.EMPTY);
  }

  /**
   * Returns the entry's key.
   *
   * @return the entry's key.
   */
  public abstract String getKey();

  /**
   * Returns the entry's value.
   *
   * @return the entry's value.
   */
  public abstract String getValue();

  /**
   * Returns the (optional) {@link EntryMetadata} associated with this {@link Entry}.
   *
   * @return the {@code EntryMetadata}.
   */
  public abstract EntryMetadata getEntryMetadata();
}
