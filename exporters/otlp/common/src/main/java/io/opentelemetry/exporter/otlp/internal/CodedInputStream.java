/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.WireFormat.FIXED32_SIZE;
import static io.opentelemetry.exporter.otlp.internal.WireFormat.FIXED64_SIZE;
import static io.opentelemetry.exporter.otlp.internal.WireFormat.MAX_VARINT_SIZE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Minimal copy of protobuf-java's CodedInputStream, currently only used in GrpcStatusUtil.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class CodedInputStream {

  private final byte[] buffer;
  private final int limit;
  private int pos;
  private int lastTag;

  /** Returns a new {@link CodedInputStream}. */
  public static CodedInputStream newInstance(byte[] serialized) {
    return new CodedInputStream(serialized);
  }

  private CodedInputStream(byte[] buffer) {
    this.buffer = buffer;
    limit = buffer.length;
    pos = 0;
  }

  /** Reads the next tag. */
  public int readTag() throws IOException {
    if (isAtEnd()) {
      lastTag = 0;
      return 0;
    }

    lastTag = readRawVarint32();
    if (WireFormat.getTagFieldNumber(lastTag) == 0) {
      // If we actually read zero (or any tag number corresponding to field
      // number zero), that's not a valid tag.
      throw new IOException("Invalid tag: " + lastTag);
    }
    return lastTag;
  }

  /** Reads a string field. */
  public String readStringRequireUtf8() throws IOException {
    final int size = readRawVarint32();
    if (size > 0 && size <= (limit - pos)) {
      String result = new String(buffer, pos, size, StandardCharsets.UTF_8);
      pos += size;
      return result;
    }

    if (size == 0) {
      return "";
    }
    if (size <= 0) {
      throw newNegativeException();
    }
    throw newTrucatedException();
  }

  /** Skips a field. */
  public boolean skipField(final int tag) throws IOException {
    switch (WireFormat.getTagWireType(tag)) {
      case WireFormat.WIRETYPE_VARINT:
        skipRawVarint();
        return true;
      case WireFormat.WIRETYPE_FIXED64:
        skipRawBytes(FIXED64_SIZE);
        return true;
      case WireFormat.WIRETYPE_LENGTH_DELIMITED:
        skipRawBytes(readRawVarint32());
        return true;
      case WireFormat.WIRETYPE_FIXED32:
        skipRawBytes(FIXED32_SIZE);
        return true;
      default:
        throw new IOException("Invalid wire type: " + tag);
    }
  }

  private boolean isAtEnd() {
    return pos == limit;
  }

  private int readRawVarint32() throws IOException {
    // See implementation notes for readRawVarint64
    fastpath:
    {
      int tempPos = pos;

      if (limit == tempPos) {
        break fastpath;
      }

      final byte[] buffer = this.buffer;
      int x;
      if ((x = buffer[tempPos++]) >= 0) {
        pos = tempPos;
        return x;
      } else if (limit - tempPos < 9) {
        break fastpath;
      } else if ((x ^= (buffer[tempPos++] << 7)) < 0) {
        x ^= (~0 << 7);
      } else if ((x ^= (buffer[tempPos++] << 14)) >= 0) {
        x ^= (~0 << 7) ^ (~0 << 14);
      } else if ((x ^= (buffer[tempPos++] << 21)) < 0) {
        x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
      } else {
        int y = buffer[tempPos++];
        x ^= y << 28;
        x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21) ^ (~0 << 28);
        if (y < 0
            && buffer[tempPos++] < 0
            && buffer[tempPos++] < 0
            && buffer[tempPos++] < 0
            && buffer[tempPos++] < 0
            && buffer[tempPos++] < 0) {
          break fastpath; // Will throw malformedVarint()
        }
      }
      pos = tempPos;
      return x;
    }
    return (int) readRawVarint64SlowPath();
  }

  private long readRawVarint64SlowPath() throws IOException {
    long result = 0;
    for (int shift = 0; shift < 64; shift += 7) {
      final byte b = readRawByte();
      result |= (long) (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        return result;
      }
    }
    throw newMalformedVarintException();
  }

  private byte readRawByte() throws IOException {
    if (pos == limit) {
      throw newTrucatedException();
    }
    return buffer[pos++];
  }

  private void skipRawVarint() throws IOException {
    if (limit - pos >= MAX_VARINT_SIZE) {
      skipRawVarintFastPath();
    } else {
      skipRawVarintSlowPath();
    }
  }

  private void skipRawVarintFastPath() throws IOException {
    for (int i = 0; i < MAX_VARINT_SIZE; i++) {
      if (buffer[pos++] >= 0) {
        return;
      }
    }
    throw newMalformedVarintException();
  }

  private void skipRawVarintSlowPath() throws IOException {
    for (int i = 0; i < MAX_VARINT_SIZE; i++) {
      if (readRawByte() >= 0) {
        return;
      }
    }
    throw newMalformedVarintException();
  }

  private void skipRawBytes(final int length) throws IOException {
    if (length >= 0 && length <= (limit - pos)) {
      // We have all the bytes we need already.
      pos += length;
      return;
    }

    if (length < 0) {
      throw newNegativeException();
    }
    throw newTrucatedException();
  }

  private static IOException newNegativeException() {
    return new IOException(
        "CodedInputStream encountered an embedded string or message "
            + "which claimed to have negative size.");
  }

  private static IOException newTrucatedException() {
    return new IOException(
        "While parsing a protocol message, the input ended unexpectedly "
            + "in the middle of a field.  This could mean either that the "
            + "input has been truncated or that an embedded message "
            + "misreported its own length.");
  }

  private static IOException newMalformedVarintException() {
    return new IOException("CodedInputStream encountered a malformed varint.");
  }
}
