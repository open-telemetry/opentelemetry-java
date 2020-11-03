/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
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
  private static final int KEY_MAX_SIZE = 256;
  private static final int VALUE_MAX_SIZE = 256;
  private static final int MAX_KEY_VALUE_PAIRS = 32;
  private static final TraceState DEFAULT = TraceState.builder().build();
  private static final int MAX_TENANT_ID_SIZE = 240;
  public static final int MAX_VENDOR_ID_SIZE = 13;

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
    for (Entry entry : getEntries()) {
      if (entry.getKey().equals(key)) {
        return entry.getValue();
      }
    }
    return null;
  }

  /** Returns the number of entries in this {@link TraceState}. */
  public int size() {
    return getEntries().size();
  }

  /** Returns whether this {@link TraceState} is empty, containing no entries. */
  public boolean isEmpty() {
    return getEntries().isEmpty();
  }

  /** Iterates over all the key-value entries contained in this {@link TraceState}. */
  public void forEach(BiConsumer<String, String> consumer) {
    for (Entry entry : getEntries()) {
      consumer.accept(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Returns a {@link List} view of the mappings contained in this {@code TraceState}.
   *
   * @return a {@link List} view of the mappings contained in this {@code TraceState}.
   */
  public abstract List<Entry> getEntries();

  /**
   * Returns a {@code Builder} based on an empty {@code TraceState}.
   *
   * @return a {@code Builder} based on an empty {@code TraceState}.
   */
  public static Builder builder() {
    return new Builder(Builder.EMPTY);
  }

  /**
   * Returns a {@code Builder} based on this {@code TraceState}.
   *
   * @return a {@code Builder} based on this {@code TraceState}.
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /** Builder class for {@link TraceState}. */
  public static final class Builder {
    private final TraceState parent;
    @Nullable private ArrayList<Entry> entries;

    // Needs to be in this class to avoid initialization deadlock because super class depends on
    // subclass (the auto-value generate class).
    private static final TraceState EMPTY = create(Collections.emptyList());

    private Builder(TraceState parent) {
      Objects.requireNonNull(parent, "parent");
      this.parent = parent;
      this.entries = null;
    }

    /**
     * Adds or updates the {@code Entry} that has the given {@code key} if it is present. The new
     * {@code Entry} will always be added in the front of the list of entries.
     *
     * @param key the key for the {@code Entry} to be added.
     * @param value the value for the {@code Entry} to be added.
     * @return this.
     */
    public Builder set(String key, String value) {
      // Initially create the Entry to validate input.
      Entry entry = Entry.create(key, value);
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
    public Builder remove(String key) {
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
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      Utils.checkArgument(validateKey(key), "Invalid key %s", key);
      Utils.checkArgument(validateValue(value), "Invalid value %s", value);
      return new AutoValue_TraceState_Entry(key, value);
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

  // Key is opaque string up to 256 characters printable. It MUST begin with a lowercase letter, and
  // can only contain lowercase letters a-z, digits 0-9, underscores _, dashes -, asterisks *, and
  // forward slashes /.  For multi-tenant vendor scenarios, an at sign (@) can be used to prefix the
  // vendor name. The tenant id (before the '@') is limited to 240 characters and the vendor id is
  // limited to 13 characters. If in the multi-tenant vendor format, then the first character
  // may additionally be digit.
  //
  // todo: benchmark this implementation
  private static boolean validateKey(String key) {
    if (key.length() > KEY_MAX_SIZE
        || key.isEmpty()
        || isNotLowercaseLetterOrDigit(key.charAt(0))) {
      return false;
    }
    boolean isMultiTenantVendorKey = false;
    for (int i = 1; i < key.length(); i++) {
      char c = key.charAt(i);
      if (isNotLegalKeyCharacter(c)) {
        return false;
      }
      if (c == '@') {
        // you can't have 2 '@' signs
        if (isMultiTenantVendorKey) {
          return false;
        }
        isMultiTenantVendorKey = true;
        // tenant id (the part to the left of the '@' sign) must be 240 characters or less
        if (i > MAX_TENANT_ID_SIZE) {
          return false;
        }
        // vendor id (the part to the right of the '@' sign) must be 13 characters or less
        if (key.length() - i > MAX_VENDOR_ID_SIZE) {
          return false;
        }
      }
    }
    if (!isMultiTenantVendorKey) {
      // if it's not the vendor format (with an '@' sign), the key must start with a letter.
      return isNotDigit(key.charAt(0));
    }
    return true;
  }

  private static boolean isNotLegalKeyCharacter(char c) {
    return isNotLowercaseLetterOrDigit(c)
        && c != '_'
        && c != '-'
        && c != '@'
        && c != '*'
        && c != '/';
  }

  private static boolean isNotLowercaseLetterOrDigit(char ch) {
    return (ch < 'a' || ch > 'z') && isNotDigit(ch);
  }

  private static boolean isNotDigit(char ch) {
    return ch < '0' || ch > '9';
  }

  // Value is opaque string up to 256 characters printable ASCII RFC0020 characters (i.e., the range
  // 0x20 to 0x7E) except comma , and =.
  private static boolean validateValue(String value) {
    if (value.length() > VALUE_MAX_SIZE || value.charAt(value.length() - 1) == ' ' /* '\u0020' */) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == ',' || c == '=' || c < ' ' /* '\u0020' */ || c > '~' /* '\u007E' */) {
        return false;
      }
    }
    return true;
  }

  private static TraceState create(List<Entry> entries) {
    Utils.checkState(entries.size() <= MAX_KEY_VALUE_PAIRS, "Invalid size");
    return new AutoValue_TraceState(Collections.unmodifiableList(entries));
  }

  TraceState() {}
}
