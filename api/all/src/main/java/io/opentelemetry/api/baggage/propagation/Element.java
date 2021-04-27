/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import java.util.BitSet;

class Element {

  private final BitSet excluded = new BitSet(128);

  private boolean leadingSpace;
  private boolean readingValue;
  private boolean trailingSpace;
  private int start;
  private int end;
  private String value;

  Element(char[] excludedChars) {
    for (char separator : excludedChars) {
      excluded.set(separator);
    }
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

  boolean tryTerminating(int i, String header) {
    if (this.readingValue) {
      markEnd(i);
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

  boolean tryNextChar(char character, int i) {
    if (isWhitespace(character)) {
      return tryNextWhitespace(i);
    } else if (isExcluded(character)) {
      return false;
    } else {
      return tryNextTokenChar(i);
    }
  }

  private static boolean isWhitespace(char character) {
    return character == ' ' || character == '\t';
  }

  private boolean isExcluded(char character) {
    return (character <= 32 || character >= 127 || excluded.get(character));
  }

  private boolean tryNextTokenChar(int i) {
    if (leadingSpace) {
      markStart(i);
    }
    return !trailingSpace;
  }

  void markStart(int start) {
    this.start = start;
    readingValue = true;
    leadingSpace = false;
  }

  private boolean tryNextWhitespace(int i) {
    if (readingValue) {
      markEnd(i);
    }
    return true;
  }
}
