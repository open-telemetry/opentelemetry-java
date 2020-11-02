/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.StringUtils;
import javax.annotation.concurrent.Immutable;

/** String-String key-value pair, along with {@link EntryMetadata}. */
@Immutable
@AutoValue
public abstract class Entry {

  // VisibleForTestig
  static final Entry INVALID = Entry.create("invalid", "");

  Entry() {}

  /**
   * Creates an {@code Entry} from the given key, value and metadata.
   *
   * @param key the entry key.
   * @param value the entry value.
   * @param entryMetadata the entry metadata.
   * @return a {@code Entry}.
   */
  public static Entry create(String key, String value, EntryMetadata entryMetadata) {
    if (!isKeyValid(key) || !isValueValid(value)) {
      return INVALID;
    }
    return new AutoValue_Entry(key, value, entryMetadata);
  }

  /**
   * Creates an {@code Entry} from the given key, value, with no metadata.
   *
   * @param key the entry key.
   * @param value the entry value.
   * @return a {@code Entry}.
   */
  public static Entry create(String key, String value) {
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

  /**
   * Determines whether the given {@code String} is a valid entry key.
   *
   * @param name the entry key name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isKeyValid(String name) {
    return name != null && !name.isEmpty() && StringUtils.isPrintableString(name);
  }

  /**
   * Determines whether the given {@code String} is a valid entry value.
   *
   * @param value the entry value to be validated.
   * @return whether the value is valid.
   */
  private static boolean isValueValid(String value) {
    return value != null && StringUtils.isPrintableString(value);
  }
}
