/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.booleanstate;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleImmediateBooleanState implements BooleanState {

  private static final VarHandle STATE_HANDLE;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      STATE_HANDLE =
          lookup.findVarHandle(VarHandleImmediateBooleanState.class, "state", boolean.class);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @SuppressWarnings("UnusedVariable") // Used by VarHandle
  private boolean state;

  public VarHandleImmediateBooleanState() {
    STATE_HANDLE.setRelease(this, false);
  }

  @Override
  public boolean get() {
    return (boolean) STATE_HANDLE.getAcquire(this);
  }

  @Override
  public void set(boolean state) {
    STATE_HANDLE.setRelease(this, state);
  }
}
