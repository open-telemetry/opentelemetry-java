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
import javax.annotation.concurrent.Immutable;

/**
 * {@link EntryKey} paired with a {@link EntryValue}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Entry {

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
  public static Entry create(EntryKey key, EntryValue value, EntryMetadata entryMetadata) {
    return new AutoValue_Entry(key, value, entryMetadata);
  }

  /**
   * Returns the entry's key.
   *
   * @return the entry's key.
   * @since 0.1.0
   */
  public abstract EntryKey getKey();

  /**
   * Returns the entry's value.
   *
   * @return the entry's value.
   * @since 0.1.0
   */
  public abstract EntryValue getValue();

  /**
   * Returns the {@link EntryMetadata} associated with this {@link Entry}.
   *
   * @return the {@code EntryMetadata}.
   * @since 0.1.0
   */
  public abstract EntryMetadata getEntryMetadata();
}
