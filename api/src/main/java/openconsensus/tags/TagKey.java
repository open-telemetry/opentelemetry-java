/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.tags;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;
import openconsensus.internal.StringUtils;
import openconsensus.internal.Utils;

/**
 * A key to a value stored in a {@link TagContext}.
 *
 * <p>Each {@code TagKey} has a {@code String} name. Names have a maximum length of {@link
 * #MAX_LENGTH} and contain only printable ASCII characters.
 *
 * <p>{@code TagKey}s are designed to be used as constants. Declaring each key as a constant
 * prevents key names from being validated multiple times.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class TagKey {
  /**
   * The maximum length for a tag key name. The value is {@value #MAX_LENGTH}.
   *
   * @since 0.1.0
   */
  public static final int MAX_LENGTH = 255;

  TagKey() {}

  /**
   * Constructs a {@code TagKey} with the given name.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @return a {@code TagKey} with the given name.
   * @throws IllegalArgumentException if the name is not valid.
   * @since 0.1.0
   */
  public static TagKey create(String name) {
    Utils.checkArgument(isValid(name), "Invalid TagKey name: %s", name);
    return new AutoValue_TagKey(name);
  }

  /**
   * Returns the name of the key.
   *
   * @return the name of the key.
   * @since 0.1.0
   */
  public abstract String getName();

  /**
   * Determines whether the given {@code String} is a valid tag key.
   *
   * @param name the tag key name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValid(String name) {
    return !name.isEmpty() && name.length() <= MAX_LENGTH && StringUtils.isPrintableString(name);
  }
}
