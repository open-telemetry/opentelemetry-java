/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

final class AnyValueBytes implements AnyValue<ByteBuffer> {

  private final byte[] raw;

  private AnyValueBytes(byte[] value) {
    this.raw = value;
  }

  static AnyValue<ByteBuffer> create(byte[] value) {
    Objects.requireNonNull(value, "value must not be null");
    return new AnyValueBytes(Arrays.copyOf(value, value.length));
  }

  @Override
  public AnyValueType getType() {
    return AnyValueType.BYTES;
  }

  @Override
  public ByteBuffer getValue() {
    return ByteBuffer.wrap(raw).asReadOnlyBuffer();
  }

  @Override
  public String asString() {
    return Base64.getEncoder().encodeToString(raw);
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
    return (o instanceof AnyValueBytes) && Arrays.equals(this.raw, ((AnyValueBytes) o).raw);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(raw);
  }
}
