/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nullable;

/** A builder of {@link TraceState}. */
public final class TraceStateBuilder {
  // Needs to be in this class to avoid initialization deadlock because super class depends on
  // subclass (the auto-value generate class).
  private static final TraceState EMPTY = TraceState.create(Collections.emptyList());

  private final TraceState parent;
  @Nullable private ArrayList<TraceState.Entry> entries;

  TraceStateBuilder() {
    parent = EMPTY;
  }

  TraceStateBuilder(TraceState parent) {
    Objects.requireNonNull(parent, "parent");
    this.parent = parent;
  }

  /**
   * Adds or updates the {@code Entry} that has the given {@code key} if it is present. The new
   * {@code Entry} will always be added in the front of the list of entries.
   *
   * @param key the key for the {@code Entry} to be added.
   * @param value the value for the {@code Entry} to be added.
   * @return this.
   */
  public TraceStateBuilder set(String key, String value) {
    // Initially create the Entry to validate input.
    TraceState.Entry entry = TraceState.Entry.create(key, value);
    if (entries == null) {
      // Copy entries from the parent.
      entries = new ArrayList<>(parent.getEntries());
    }
    for (int i = 0; i < entries.size(); i++) {
      if (entries.get(i).getKey().equals(entry.getKey())) {
        entries.remove(i);
        // Exit now because the entries list cannot contain duplicates.
        break;
      }
    }
    // Inserts the element at the front of this list.
    entries.add(0, entry);
    return this;
  }

  /**
   * Removes the {@code Entry} that has the given {@code key} if it is present.
   *
   * @param key the key for the {@code Entry} to be removed.
   * @return this.
   */
  public TraceStateBuilder remove(String key) {
    Objects.requireNonNull(key, "key");
    if (entries == null) {
      // Copy entries from the parent.
      entries = new ArrayList<>(parent.getEntries());
    }
    for (int i = 0; i < entries.size(); i++) {
      if (entries.get(i).getKey().equals(key)) {
        entries.remove(i);
        // Exit now because the entries list cannot contain duplicates.
        break;
      }
    }
    return this;
  }

  /**
   * Builds a TraceState by adding the entries to the parent in front of the key-value pairs list
   * and removing duplicate entries.
   *
   * @return a TraceState with the new entries.
   */
  public TraceState build() {
    if (entries == null) {
      return parent;
    }
    return TraceState.create(entries);
  }
}
