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

package io.opentelemetry.distributedcontext;

import com.google.auto.value.AutoValue;
import io.opentelemetry.distributedcontext.LabelMetadata.HopLimit;
import javax.annotation.concurrent.Immutable;

/**
 * {@link LabelKey} paired with a {@link LabelValue}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Label {

  /** Default propagation metadata - unlimited propagation. */
  public static final LabelMetadata METADATA_UNLIMITED_PROPAGATION =
      LabelMetadata.create(HopLimit.UNLIMITED_PROPAGATION);

  Label() {}

  /**
   * Creates an {@code Entry} from the given key, value and metadata.
   *
   * @param key the entry key.
   * @param value the entry value.
   * @param entryMetadata the entry metadata.
   * @return a {@code Entry}.
   * @since 0.1.0
   */
  public static Label create(LabelKey key, LabelValue value, LabelMetadata entryMetadata) {
    return new AutoValue_Label(key, value, entryMetadata);
  }

  /**
   * Returns the entry's key.
   *
   * @return the entry's key.
   * @since 0.1.0
   */
  public abstract LabelKey getKey();

  /**
   * Returns the entry's value.
   *
   * @return the entry's value.
   * @since 0.1.0
   */
  public abstract LabelValue getValue();

  /**
   * Returns the {@link LabelMetadata} associated with this {@link Label}.
   *
   * @return the {@code EntryMetadata}.
   * @since 0.1.0
   */
  public abstract LabelMetadata getEntryMetadata();
}
