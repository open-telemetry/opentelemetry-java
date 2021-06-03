/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import java.util.BitSet;

/**
 * Represents single element of a W3C baggage header (key or value). Allows tracking parsing of a
 * header string, keeping the state and validating allowed characters. Parsing state can be reset
 * with {@link #reset(int)} allowing instance re-use.
 */
class Element {

  private static final BitSet EXCLUDED_KEY_CHARS = new BitSet(128);
  private static final BitSet EXCLUDED_VALUE_CHARS = new BitSet(128);

  static {
    for (char c :
        new char[] {
          '(', ')', '<', '>', '@', ',', ';', ':', '\\', '"', '/', '[', ']', '?', '=', '{', '}'
        }) {
      EXCLUDED_KEY_CHARS.set(c);
    }
    for (char c : new char[] {'"', ',', ';', '\\'}) {
      EXCLUDED_VALUE_CHARS.set(c);
    }
  }

  private final BitSet excluded;

  private boolean leadingSpace;
  private boolean readingValue;
  private boolean trailingSpace;
  private int start;
  private int end;
  private String value;

  static Element createKeyElement() {
    return new Element(EXCLUDED_KEY_CHARS);
  }

  static Element createValueElement() {
    return new Element(EXCLUDED_VALUE_CHARS);
  }

  /**
   * Constructs element instance.
   *
   * @param excluded characters that are not allowed for this type of an element
   */
  Element(BitSet excluded) {
    this.excluded = excluded;
    reset(0);
  }

  String getValue() {
    return value;
  }

  void reset(int start) {
    this.start = start;
    leadingSpace = true;
    readingValue = false;
    trailingSpace = false;
    value = null;
  }

  boolean tryTerminating(int index, String header) {
    if (this.readingValue) {
      markEnd(index);
    }
    if (this.trailingSpace) {
      setValue(header);
      return true;
    } else {
      // leading spaces - no content, invalid
      return false;
    }
  }

  private void markEnd(int end) {
    this.end = end;
    this.readingValue = false;
    trailingSpace = true;
  }

  private void setValue(String header) {
    this.value = header.substring(this.start, this.end);
  }

  boolean tryNextChar(char character, int index) {
    if (isWhitespace(character)) {
      return tryNextWhitespace(index);
    } else if (isExcluded(character)) {
      return false;
    } else {
      return tryNextTokenChar(index);
    }
  }

  private static boolean isWhitespace(char character) {
    return character == ' ' || character == '\t';
  }

  private boolean tryNextWhitespace(int index) {
    if (readingValue) {
      markEnd(index);
    }
    return true;
  }

  private boolean isExcluded(char character) {
    return (character <= 32 || character >= 127 || excluded.get(character));
  }

  private boolean tryNextTokenChar(int index) {
    if (leadingSpace) {
      markStart(index);
    }
    return !trailingSpace;
  }

  private void markStart(int start) {
    this.start = start;
    readingValue = true;
    leadingSpace = false;
  }
}
