/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import javax.annotation.Nullable;

/**
 * StringEncoder implementation using VarHandle for high performance on Java 9+.
 *
 * <p>This implementation provides optimized string operations by directly accessing String internal
 * fields using VarHandle operations. It's only created if VarHandle is available and all required
 * handles can be resolved.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class VarHandleStringEncoder extends AbstractStringEncoder {

  private final VarHandle valueHandle;
  private final VarHandle coderHandle;
  private static final VarHandle LONG_ARRAY_HANDLE =
      MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);

  private VarHandleStringEncoder(VarHandle valueHandle, VarHandle coderHandle) {
    this.valueHandle = valueHandle;
    this.coderHandle = coderHandle;
  }

  @Nullable
  public static VarHandleStringEncoder createIfAvailable() {
    VarHandle valueHandle1 = getStringFieldHandle("value", byte[].class);
    VarHandle coderHandle1 = getStringFieldHandle("coder", byte.class);

    if (valueHandle1 == null || coderHandle1 == null) {
      return null;
    }

    return new VarHandleStringEncoder(valueHandle1, coderHandle1);
  }

  @Override
  protected byte[] getStringBytes(String string) {
    return (byte[]) valueHandle.get(string);
  }

  @Override
  protected boolean isLatin1(String string) {
    return ((byte) coderHandle.get(string)) == 0;
  }

  @Override
  protected long getLong(byte[] bytes, int offset) {
    return (long) LONG_ARRAY_HANDLE.get(bytes, offset);
  }

  @Nullable
  private static VarHandle getStringFieldHandle(String fieldName, Class<?> expectedType) {
    try {
      Field field = String.class.getDeclaredField(fieldName);
      if (!expectedType.isAssignableFrom(field.getType())) {
        return null;
      }

      MethodHandles.Lookup lookup =
          MethodHandles.privateLookupIn(String.class, MethodHandles.lookup());
      return lookup.findVarHandle(String.class, fieldName, expectedType);
    } catch (Exception exception) {
      return null;
    }
  }
}
