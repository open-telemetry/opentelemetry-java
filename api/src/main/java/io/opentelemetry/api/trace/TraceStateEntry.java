/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.Utils;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/** Immutable key-value pair for {@code TraceState}. */
@Immutable
@AutoValue
public abstract class TraceStateEntry {
  private static final int KEY_MAX_SIZE = 256;
  private static final int VALUE_MAX_SIZE = 256;
  private static final int MAX_TENANT_ID_SIZE = 240;
  public static final int MAX_VENDOR_ID_SIZE = 13;

  /**
   * Creates a new {@code Entry} for the {@code TraceState}.
   *
   * @param key the Entry's key.
   * @param value the Entry's value.
   * @return the new {@code Entry}.
   */
  public static TraceStateEntry create(String key, String value) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(value, "value");
    Utils.checkArgument(validateKey(key), "Invalid key %s", key);
    Utils.checkArgument(validateValue(value), "Invalid value %s", value);
    return new AutoValue_TraceStateEntry(key, value);
  }

  /**
   * Returns the key {@code String}.
   *
   * @return the key {@code String}.
   */
  public abstract String getKey();

  /**
   * Returns the value {@code String}.
   *
   * @return the value {@code String}.
   */
  public abstract String getValue();

  TraceStateEntry() {}

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
}
