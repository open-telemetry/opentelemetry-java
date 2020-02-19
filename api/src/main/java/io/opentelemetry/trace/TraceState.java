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

package io.opentelemetry.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Carries tracing-system specific context in a list of key-value pairs. TraceState allows different
 * vendors propagate additional information and inter-operate with their legacy Id formats.
 *
 * <p>Implementation is optimized for a small list of key-value pairs.
 *
 * <p>Key is opaque string up to 256 characters printable. It MUST begin with a lowercase letter,
 * and can only contain lowercase letters a-z, digits 0-9, underscores _, dashes -, asterisks *, and
 * forward slashes /.
 *
 * <p>Value is opaque string up to 256 characters printable ASCII RFC0020 characters (i.e., the
 * range 0x20 to 0x7E) except comma , and =.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class TraceState {
  private static final TraceState DEFAULT = TraceState.builder().build();

  /**
   * Returns the default {@code TraceState} with no entries.
   *
   * @return the default {@code TraceState}.
   * @since 0.1.0
   */
  public static TraceState getDefault() {
    return DEFAULT;
  }

  /**
   * Returns a {@link List} view of the mappings contained in this {@code TraceState}.
   *
   * @return a {@link List} view of the mappings contained in this {@code TraceState}.
   * @since 0.1.0
   */
  public abstract List<String> getEntries();

  /**
   * Returns a {@code Builder} based on an empty {@code TraceState}.
   *
   * @return a {@code Builder} based on an empty {@code TraceState}.
   * @since 0.1.0
   */
  public static Builder builder() {
    return new Builder(Builder.EMPTY);
  }

  /**
   * Returns a {@code Builder} based on this {@code TraceState}.
   *
   * @return a {@code Builder} based on this {@code TraceState}.
   * @since 0.1.0
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /**
   * Builder class for {@link TraceState}.
   *
   * @since 0.1.0
   */
  public static final class Builder {
    private final TraceState parent;
    @Nullable private ArrayList<String> entries;

    // Needs to be in this class to avoid initialization deadlock because super class depends on
    // subclass (the auto-value generate class).
    private static final TraceState EMPTY = create(Collections.<String>emptyList());

    private Builder(TraceState parent) {
      Utils.checkNotNull(parent, "parent");
      this.parent = parent;
      this.entries = null;
    }

    /**
     * Adds an entry to the TraceState. The new value will always be added in the front of the list
     * of entries.
     *
     * @param entry the value to be added.
     * @return this.
     * @since 0.1.0
     */
    public Builder add(String entry) {
      Utils.checkNotNull(entry, "entry");
      if (entries == null) {
        // Copy entries from the parent.
        entries = new ArrayList<>(parent.getEntries());
      }
      // Inserts the element at the front of this list.
      entries.add(0, entry);
      return this;
    }

    /**
     * Builds a TraceState by adding the entries to the parent in front of the key-value pairs list
     * and removing duplicate entries.
     *
     * @return a TraceState with the new entries.
     * @since 0.1.0
     */
    public TraceState build() {
      if (entries == null) {
        return parent;
      }
      return TraceState.create(entries);
    }
  }

  private static TraceState create(List<String> entries) {
    return new AutoValue_TraceState(Collections.unmodifiableList(entries));
  }

  TraceState() {}
}
