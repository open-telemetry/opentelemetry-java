/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

final class ValueBytes implements Value<ByteBuffer> {

  private final byte[] raw;

  private ValueBytes(byte[] value) {
    this.raw = value;
  }

  static Value<ByteBuffer> create(byte[] value) {
    Objects.requireNonNull(value, "value must not be null");
    return new ValueBytes(Arrays.copyOf(value, value.length));
  }

  @Override
  public ValueType getType() {
    return ValueType.BYTES;
  }

  @Override
  public ByteBuffer getValue() {
    return ByteBuffer.wrap(raw).asReadOnlyBuffer();
  }

  @Override
  public String asString() {
    StringBuilder sb = new StringBuilder();
    ProtoJson.append(sb, this);
    return sb.toString();
  }

  @Override
  public String toString() {
    return "ValueBytes{" + asString() + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return (o instanceof ValueBytes) && Arrays.equals(this.raw, ((ValueBytes) o).raw);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(raw);
  }
}
