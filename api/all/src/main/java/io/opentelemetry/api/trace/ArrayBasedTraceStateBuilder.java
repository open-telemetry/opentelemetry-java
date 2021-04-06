/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.internal.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

final class ArrayBasedTraceStateBuilder implements TraceStateBuilder {

  private static final int MAX_VENDOR_ID_SIZE = 13;

  // Needs to be in this class to avoid initialization deadlock because super class depends on
  // subclass (the auto-value generate class).
  private static final ArrayBasedTraceState EMPTY =
      ArrayBasedTraceState.create(Collections.emptyList());

  private static final int MAX_KEY_VALUE_PAIRS = 32;
  private static final int KEY_MAX_SIZE = 256;
  private static final int VALUE_MAX_SIZE = 256;
  private static final int MAX_TENANT_ID_SIZE = 240;

  private final ArrayBasedTraceState parent;
  @Nullable private List<String> entries;

  static TraceState empty() {
    return EMPTY;
  }

  ArrayBasedTraceStateBuilder() {
    parent = EMPTY;
  }

  ArrayBasedTraceStateBuilder(ArrayBasedTraceState parent) {
    this.parent = parent;
  }

  @Override
  public TraceStateBuilder put(String key, String value) {
    if (!isKeyValid(key)
        || !isValueValid(value)
        || (entries != null && entries.size() >= MAX_KEY_VALUE_PAIRS)) {
      return this;
    }
    if (entries == null) {
      // Copy entries from the parent.
      entries = new ArrayList<>(parent.getEntries());
    }
    removeEntry(key);
    // Inserts the element at the front of this list. (note: probably pretty inefficient with an
    // ArrayList as the underlying implementation!)
    entries.add(0, key);
    entries.add(1, value);
    return this;
  }

  @Override
  public TraceStateBuilder remove(String key) {
    if (key == null) {
      return this;
    }
    if (entries == null) {
      // Copy entries from the parent.
      entries = new ArrayList<>(parent.getEntries());
    }
    removeEntry(key);
    return this;
  }

  private void removeEntry(String key) {
    int currentSize = entries.size();
    for (int i = 0; i < currentSize; i += 2) {
      if (entries.get(i).equals(key)) {
        // remove twice at i to get the key & the value (yes, this is pretty ugly).
        entries.remove(i);
        entries.remove(i);
        // Exit now because the entries list cannot contain duplicates.
        break;
      }
    }
  }

  @Override
  public TraceState build() {
    if (entries == null) {
      return parent;
    }
    return ArrayBasedTraceState.create(entries);
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
        // vendor id (the part to the right of the '@' sign) must be 1-13 characters long
        int remainingKeyChars = key.length() - i - 1;
        if (remainingKeyChars > MAX_VENDOR_ID_SIZE || remainingKeyChars == 0) {
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
    if (StringUtils.isNullOrEmpty(value)) {
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
