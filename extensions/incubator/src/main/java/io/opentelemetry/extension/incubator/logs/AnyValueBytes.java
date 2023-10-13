/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

import io.opentelemetry.api.internal.OtelEncodingUtils;
import java.util.Arrays;
import java.util.Objects;

final class AnyValueBytes implements AnyValue<byte[]> {

  private final byte[] value;

  private AnyValueBytes(byte[] value) {
    this.value = value;
  }

  static AnyValue<byte[]> create(byte[] value) {
    Objects.requireNonNull(value, "value must not be null");
    return new AnyValueBytes(Arrays.copyOf(value, value.length));
  }

  @Override
  public AnyValueType getType() {
    return AnyValueType.BYTES;
  }

  @Override
  public byte[] getValue() {
    return value;
  }

  @Override
  public String asString() {
    // TODO: base64 would be better, but isn't available in android and java. Can we vendor in a
    // base64 implementation?
    char[] arr = new char[value.length * 2];
    OtelEncodingUtils.bytesToBase16(value, arr, value.length);
    return new String(arr);
  }

  @Override
  public String toString() {
    return "AnyValueBytes{" + asString() + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return (o instanceof AnyValue)
        && ((AnyValue<?>) o).getType() == AnyValueType.BYTES
        && Arrays.equals(this.value, (byte[]) ((AnyValue<?>) o).getValue());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }
}
