/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.correlationcontext;

import com.google.auto.value.AutoValue;
import io.opentelemetry.correlationcontext.EntryMetadata.EntryTtl;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.internal.Utils;
import javax.annotation.concurrent.Immutable;

/**
 * String-String key-value pair, along with {@link EntryMetadata}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Entry {
  /**
   * The maximum length for an entry key name. The value is {@value #MAX_KEY_LENGTH}.
   *
   * @since 0.1.0
   */
  public static final int MAX_KEY_LENGTH = 255;

  /**
   * The maximum length for a entry value. The value is {@value #MAX_LENGTH}.
   *
   * @since 0.1.0
   */
  public static final int MAX_VALUE_LENGTH = 255;

  /** Default propagation metadata - unlimited propagation. */
  public static final EntryMetadata METADATA_UNLIMITED_PROPAGATION =
      EntryMetadata.create(EntryTtl.UNLIMITED_PROPAGATION);

  Entry() {}

  /**
   * Creates an {@code Entry} from the given key, value and metadata.
   *
   * @param key the entry key.
   * @param value the entry value.
   * @param entryMetadata the entry metadata.
   * @return a {@code Entry}.
   * @since 0.1.0
   */
  public static Entry create(String key, String value, EntryMetadata entryMetadata) {
    Utils.checkArgument(keyIsValid(key), "Invalid entry key name: %s", key);
    Utils.checkArgument(isValueValid(value), "Invalid entry value: %s", value);
    return new AutoValue_Entry(key, value, entryMetadata);
  }

  /**
   * Returns the entry's key.
   *
   * @return the entry's key.
   * @since 0.1.0
   */
  public abstract String getKey();

  /**
   * Returns the entry's value.
   *
   * @return the entry's value.
   * @since 0.1.0
   */
  public abstract String getValue();

  /**
   * Returns the {@link EntryMetadata} associated with this {@link Entry}.
   *
   * @return the {@code EntryMetadata}.
   * @since 0.1.0
   */
  public abstract EntryMetadata getEntryMetadata();

  /**
   * Determines whether the given {@code String} is a valid entry key.
   *
   * @param name the entry key name to be validated.
   * @return whether the name is valid.
   */
  private static boolean keyIsValid(String name) {
    return !name.isEmpty()
        && name.length() <= MAX_KEY_LENGTH
        && StringUtils.isPrintableString(name);
  }

  /**
   * Determines whether the given {@code String} is a valid entry value.
   *
   * @param value the entry value to be validated.
   * @return whether the value is valid.
   */
  private static boolean isValueValid(String value) {
    return value.length() <= MAX_VALUE_LENGTH && StringUtils.isPrintableString(value);
  }
}
