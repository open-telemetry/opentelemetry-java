/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.io.Closeable;
import java.io.IOException;

/**
 * Abstraction over JSON streaming writers, allowing different JSON libraries (e.g. Jackson 2,
 * Jackson 3) to be used interchangeably.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface JsonWriter extends Closeable {

  void writeStartObject() throws IOException;

  void writeEndObject() throws IOException;

  void writeStartArray() throws IOException;

  void writeEndArray() throws IOException;

  void writeFieldName(String name) throws IOException;

  void writeString(String value) throws IOException;

  void writeStringField(String fieldName, String value) throws IOException;

  void writeNumber(int value) throws IOException;

  void writeNumber(long value) throws IOException;

  void writeNumber(double value) throws IOException;

  void writeNumberField(String fieldName, int value) throws IOException;

  void writeNumberField(String fieldName, long value) throws IOException;

  void writeNumberField(String fieldName, double value) throws IOException;

  void writeBoolean(boolean value) throws IOException;

  void writeBooleanField(String fieldName, boolean value) throws IOException;

  void writeBinary(byte[] data) throws IOException;

  void writeBinaryField(String fieldName, byte[] data) throws IOException;

  void writeObjectFieldStart(String fieldName) throws IOException;

  void writeArrayFieldStart(String fieldName) throws IOException;

  void writeRaw(String text) throws IOException;

  void writeRaw(char c) throws IOException;

  void flush() throws IOException;
}
