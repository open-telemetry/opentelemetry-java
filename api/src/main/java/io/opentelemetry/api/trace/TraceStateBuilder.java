/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nullable;

/** A builder of {@link TraceStateImpl}. */
public final class TraceStateBuilder {
  public static final int MAX_VENDOR_ID_SIZE = 13;

  // Needs to be in this class to avoid initialization deadlock because super class depends on
  // subclass (the auto-value generate class).
  private static final TraceStateImpl EMPTY = TraceStateImpl.create(Collections.emptyList());

  private static final int MAX_KEY_VALUE_PAIRS = 32;
  private static final int KEY_MAX_SIZE = 256;
  private static final int VALUE_MAX_SIZE = 256;
  private static final int MAX_TENANT_ID_SIZE = 240;

  private final TraceStateImpl parent;
  @Nullable private ArrayList<TraceStateImpl.Entry> entries;

  TraceStateBuilder() {
    parent = EMPTY;
  }

  TraceStateBuilder(TraceStateImpl parent) {
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
    if (!isKeyValid(key)
        || !isValueValid(value)
        || (entries != null && entries.size() >= MAX_KEY_VALUE_PAIRS)) {
      return this;
    }
    // Initially create the Entry to validate input.
    TraceStateImpl.Entry entry = TraceStateImpl.Entry.create(key, value);
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
    if (key == null) {
      return this;
    }
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
  public TraceStateImpl build() {
    if (entries == null) {
      return parent;
    }
    return TraceStateImpl.create(entries);
  }

  // Key is opaque string up to 256 characters printable. It MUST begin with a lowercase letter, and
  // can only contain lowercase letters a-z, digits 0-9, underscores _, dashes -, asterisks *, and
  // forward slashes /.  For multi-tenant vendor scenarios, an at sign (@) can be used to prefix the
  // vendor name. The tenant id (before the '@') is limited to 240 characters and the vendor id is
  // limited to 13 characters. If in the multi-tenant vendor format, then the first character
  // may additionally be digit.
  //
  // todo: benchmark this implementation
  private static boolean isKeyValid(@Nullable String key) {
    if (key == null) {
      return false;
    }
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
  private static boolean isValueValid(@Nullable String value) {
    if (value == null) {
      return false;
    }
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
}
