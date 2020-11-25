/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class TraceStateImpl implements TraceState {

  @Override
  @Nullable
  public String get(String key) {
    for (Entry entry : getEntries()) {
      if (entry.getKey().equals(key)) {
        return entry.getValue();
      }
    }
    return null;
  }

  @Override
  public int size() {
    return getEntries().size();
  }

  @Override
  public boolean isEmpty() {
    return getEntries().isEmpty();
  }

  @Override
  public void forEach(BiConsumer<String, String> consumer) {
    for (Entry entry : getEntries()) {
      consumer.accept(entry.getKey(), entry.getValue());
    }
  }

  abstract List<Entry> getEntries();

  @Override
  public TraceStateBuilder toBuilder() {
    return new TraceStateBuilder(this);
  }

  static TraceStateImpl create(List<Entry> entries) {
    return new AutoValue_TraceStateImpl(Collections.unmodifiableList(entries));
  }

  TraceStateImpl() {}

  /** Immutable key-value pair for {@code TraceState}. */
  @Immutable
  @AutoValue
  abstract static class Entry {
    /**
     * Creates a new {@code Entry} for the {@code TraceState}.
     *
     * @param key the Entry's key.
     * @param value the Entry's value.
     * @return the new {@code Entry}.
     */
    // Visible for testing
    static Entry create(String key, String value) {
      return new AutoValue_TraceStateImpl_Entry(key, value);
    }

    /**
     * Returns the key {@code String}.
     *
     * @return the key {@code String}.
     */
    abstract String getKey();

    /**
     * Returns the value {@code String}.
     *
     * @return the value {@code String}.
     */
    abstract String getValue();

    Entry() {}
  }
}
