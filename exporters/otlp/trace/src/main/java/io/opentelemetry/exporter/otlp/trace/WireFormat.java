/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

// Copied from
// https://github.com/protocolbuffers/protobuf/blob/master/java/core/src/main/java/com/google/protobuf/WireFormat.java
//
// Unneeded lines of code are deleted as is, without any modifications otherwise.
final class WireFormat {

  static final int FIXED32_SIZE = 4;
  static final int FIXED64_SIZE = 8;
  static final int MAX_VARINT32_SIZE = 5;
  static final int MAX_VARINT64_SIZE = 10;
  static final int MAX_VARINT_SIZE = 10;

  static final int WIRETYPE_VARINT = 0;
  static final int WIRETYPE_FIXED64 = 1;
  static final int WIRETYPE_LENGTH_DELIMITED = 2;
  static final int WIRETYPE_FIXED32 = 5;

  static final int TAG_TYPE_BITS = 3;
  static final int TAG_TYPE_MASK = (1 << TAG_TYPE_BITS) - 1;

  /** Makes a tag value given a field number and wire type. */
  static int makeTag(final int fieldNumber, final int wireType) {
    return (fieldNumber << TAG_TYPE_BITS) | wireType;
  }

  private WireFormat() {}
}
