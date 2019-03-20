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
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * The value of a {@code Label} associated with a {@code TimeSeries}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
@Immutable
@AutoValue
public abstract class LabelValue {

  LabelValue() {}

  /**
   * Creates a {@link LabelValue}.
   *
   * @param value the value of a {@code Label}. {@code null} value indicates an unset {@code
   *     LabelValue}.
   * @return a {@code LabelValue}.
   * @since 0.1.0
   */
  public static LabelValue create(@Nullable String value) {
    return new AutoValue_LabelValue(value);
  }

  /**
   * Returns the value of this {@link LabelValue}. Returns {@code null} if the value is unset and
   * supposed to be ignored.
   *
   * @return the value.
   * @since 0.1.0
   */
  @Nullable
  public abstract String getValue();
}
