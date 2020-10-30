/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.internal.Utils;
import javax.annotation.concurrent.Immutable;

/** String-String key-value pair, along with {@link EntryMetadata}. */
@Immutable
public final class Entry {

  /**
   * Creates an {@code Entry} from the given key, value and metadata.
   *
   * @param key the entry key.
   * @param value the entry value.
   * @param entryMetadata the entry metadata.
   * @return a {@code Entry}.
   */
  public static Entry create(String key, String value, EntryMetadata entryMetadata) {
    Utils.checkArgument(keyIsValid(key), "Invalid entry key name: %s", key);
    Utils.checkArgument(isValueValid(value), "Invalid entry value: %s", value);
    return new Entry(key, value, entryMetadata);
  }

  /**
   * Creates an {@code Entry} from the given key, value, with no metadata.
   *
   * @param key the entry key.
   * @param value the entry value.
   * @return a {@code Entry}.
   */
  public static Entry create(String key, String value) {
    return create(key, value, EntryMetadata.empty());
  }

  private final String key;
  private final String value;
  private final EntryMetadata entryMetadata;

  private Entry(String key, String value, EntryMetadata entryMetadata) {
    this.key = key;
    this.value = value;
    this.entryMetadata = entryMetadata;
  }

  /**
   * Returns the entry's key.
   *
   * @return the entry's key.
   */
  public String getKey() {
    return key;
  }

  /**
   * Returns the entry's value.
   *
   * @return the entry's value.
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the (optional) {@link EntryMetadata} associated with this {@link Entry}.
   *
   * @return the {@code EntryMetadata}.
   */
  public EntryMetadata getEntryMetadata() {
    return entryMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Entry)) {
      return false;
    }
    Entry entry = (Entry) o;
    return key.equals(entry.key)
        && value.equals(entry.value)
        && entryMetadata.equals(entry.entryMetadata);
  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + value.hashCode();
    result = 31 * result + entryMetadata.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Entry{"
        + "key='"
        + key
        + '\''
        + ", value='"
        + value
        + '\''
        + ", entryMetadata="
        + entryMetadata
        + '}';
  }

  /**
   * Determines whether the given {@code String} is a valid entry key.
   *
   * @param name the entry key name to be validated.
   * @return whether the name is valid.
   */
  private static boolean keyIsValid(String name) {
    return !name.isEmpty() && StringUtils.isPrintableString(name);
  }

  /**
   * Determines whether the given {@code String} is a valid entry value.
   *
   * @param value the entry value to be validated.
   * @return whether the value is valid.
   */
  private static boolean isValueValid(String value) {
    return StringUtils.isPrintableString(value);
  }
}
