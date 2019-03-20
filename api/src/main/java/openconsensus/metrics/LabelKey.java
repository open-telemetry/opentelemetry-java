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

package openconsensus.metrics;

import com.google.auto.value.AutoValue;
import openconsensus.common.ExperimentalApi;
import javax.annotation.concurrent.Immutable;

/**
 * The key of a {@code Label} associated with a {@code MetricDescriptor}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
@Immutable
@AutoValue
public abstract class LabelKey {

  LabelKey() {}

  /**
   * Creates a {@link LabelKey}.
   *
   * @param key the key of a {@code Label}.
   * @param description a human-readable description of what this label key represents.
   * @return a {@code LabelKey}.
   * @since 0.1.0
   */
  public static LabelKey create(String key, String description) {
    return new AutoValue_LabelKey(key, description);
  }

  /**
   * Returns the key of this {@link LabelKey}.
   *
   * @return the key.
   * @since 0.1.0
   */
  public abstract String getKey();

  /**
   * Returns the description of this {@link LabelKey}.
   *
   * @return the description.
   * @since 0.1.0
   */
  public abstract String getDescription();
}
