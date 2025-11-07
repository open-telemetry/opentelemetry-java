/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.internal.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

final class ArrayBasedTraceStateBuilder implements TraceStateBuilder {

  private static final int MAX_VENDOR_ID_SIZE = 14;

  // Needs to be in this class to avoid initialization deadlock because super class depends on
  // subclass (the auto-value generate class).
  private static final ArrayBasedTraceState EMPTY =
      ArrayBasedTraceState.create(Collections.emptyList());

  private static final int MAX_ENTRIES = 32;
  private static final int KEY_MAX_SIZE = 256;
  private static final int VALUE_MAX_SIZE = 256;
  private static final int MAX_TENANT_ID_SIZE = 241;

  // Later calls to put must be at the front of trace state. We append to the list and then reverse
  // when finished.
  private final List<String> reversedEntries;

  // We record removed entries with null values, this is the number of entries in the builder not
  // including removed ones.
  int numEntries;

  static TraceState empty() {
    return EMPTY;
  }

  ArrayBasedTraceStateBuilder() {
    reversedEntries = new ArrayList<>();
    numEntries = 0;
  }

  ArrayBasedTraceStateBuilder(ArrayBasedTraceState parent) {
    List<String> entries = parent.getEntries();
    int size = entries.size();
    reversedEntries = new ArrayList<>(size);
    for (int i = size - 2; i >= 0; i -= 2) {
      reversedEntries.add(entries.get(i));
      reversedEntries.add(entries.get(i + 1));
    }
    numEntries = size / 2;
  }

  /**
   * Allows key value pairs to be added to the TraceState.
   *
   * @param key is an opaque string up to 256 characters printable. It MUST begin with a lowercase
   *     letter, and can only contain lowercase letters a-z, digits 0-9, underscores _, dashes -,
   *     asterisks *, and forward slashes /. For multi-tenant vendor scenarios, an at sign (@) can
   *     be used to prefix the vendor name. The tenant id (before the '@') is limited to 240
   *     characters and the vendor id is limited to 13 characters. If in the multi-tenant vendor
   *     format, then the first character may additionally be numeric.
   */
  @Override
  public TraceStateBuilder put(String key, String value) {
    if (!isKeyValid(key) || !isValueValid(value) || numEntries >= MAX_ENTRIES) {
      return this;
    }
    for (int i = 0; i < reversedEntries.size(); i += 2) {
      if (reversedEntries.get(i).equals(key)) {
        String currentValue = reversedEntries.get(i + 1);
        reversedEntries.set(i + 1, value);
        if (currentValue == null) {
          numEntries++;
        }
        return this;
      }
    }
    reversedEntries.add(key);
    reversedEntries.add(value);
    numEntries++;
    return this;
  }

  @Override
  public TraceStateBuilder remove(String key) {
    if (key == null) {
      return this;
    }
    for (int i = 0; i < reversedEntries.size(); i += 2) {
      if (reversedEntries.get(i).equals(key)) {
        reversedEntries.set(i + 1, null);
        numEntries--;
        return this;
      }
    }
    return this;
  }

  @Override
  public TraceState build() {
    if (numEntries == 0) {
      return empty();
    }

    if (reversedEntries.size() == 2) {
      return ArrayBasedTraceState.create(new ArrayList<>(reversedEntries));
    }
    String[] entries = new String[numEntries * 2];
    int pos = 0;
    for (int i = reversedEntries.size() - 2; i >= 0; i -= 2) {
      String key = reversedEntries.get(i);
      String value = reversedEntries.get(i + 1);
      if (value != null) {
        entries[pos++] = key;
        entries[pos++] = value;
      }
    }
    // TODO(anuraaga): We may consider removing AutoValue which prevents us from storing the array
    // directly as it would be a bit more performant, though not hugely.
    return ArrayBasedTraceState.create(Arrays.asList(entries));
  }

  /**
   * Checks the validity of a key.
   *
   * @param key is an opaque string up to 256 characters printable. It MUST begin with a lowercase
   *     letter, and can only contain lowercase letters a-z, digits 0-9, underscores _, dashes -,
   *     asterisks *, and forward slashes /. For multi-tenant vendor scenarios, an at sign (@) can
   *     be used to prefix the vendor name. The tenant id (before the '@') is limited to 240
   *     characters and the vendor id is limited to 13 characters. If in the multi-tenant vendor
   *     format, then the first character may additionally be numeric.
   * @return boolean representing key validity
   */
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
        // tenant id (the part to the left of the '@' sign) must be 241 characters or less
        if (i > MAX_TENANT_ID_SIZE) {
          return false;
        }
        // vendor id (the part to the right of the '@' sign) must be 1-14 characters long
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
