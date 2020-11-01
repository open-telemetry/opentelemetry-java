/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.Utils;
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
 */
@Immutable
@AutoValue
public abstract class TraceState {
  private static final int MAX_KEY_VALUE_PAIRS = 32;

  private static final TraceState DEFAULT = TraceState.builder().build();

  /**
   * Returns the default {@code TraceState} with no entries.
   *
   * @return the default {@code TraceState}.
   */
  public static TraceState getDefault() {
    return DEFAULT;
  }

  /**
   * Returns the value to which the specified key is mapped, or null if this map contains no mapping
   * for the key.
   *
   * @param key with which the specified value is to be associated
   * @return the value to which the specified key is mapped, or null if this map contains no mapping
   *     for the key.
   */
  @Nullable
  public String get(String key) {
    for (TraceStateEntry entry : getEntries()) {
      if (entry.getKey().equals(key)) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * Returns a {@link List} view of the mappings contained in this {@code TraceState}.
   *
   * @return a {@link List} view of the mappings contained in this {@code TraceState}.
   */
  public abstract List<TraceStateEntry> getEntries();

  /**
   * Returns a {@code Builder} based on an empty {@code TraceState}.
   *
   * @return a {@code Builder} based on an empty {@code TraceState}.
   */
  public static TraceStateBuilder builder() {
    return new TraceStateBuilder();
  }

  /**
   * Returns a {@code Builder} based on this {@code TraceState}.
   *
   * @return a {@code Builder} based on this {@code TraceState}.
   */
  public TraceStateBuilder toBuilder() {
    return new TraceStateBuilder(this);
  }

  static TraceState create(List<TraceStateEntry> entries) {
    Utils.checkState(entries.size() <= MAX_KEY_VALUE_PAIRS, "Invalid size");
    return new AutoValue_TraceState(Collections.unmodifiableList(entries));
  }

  TraceState() {}
}
