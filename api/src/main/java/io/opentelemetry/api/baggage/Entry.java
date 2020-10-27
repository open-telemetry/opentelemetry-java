/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.internal.Utils;
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
  public static Entry create(String key, String value, EntryMetadata entryMetadata) {
    Utils.checkArgument(keyIsValid(key), "Invalid entry key name: %s", key);
    Utils.checkArgument(isValueValid(value), "Invalid entry value: %s", value);
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
