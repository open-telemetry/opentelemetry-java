/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.booleanstate;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleEventualBooleanState implements BooleanState {

  private static final VarHandle STATE_HANDLE;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      STATE_HANDLE =
          lookup.findVarHandle(VarHandleEventualBooleanState.class, "state", boolean.class);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @SuppressWarnings("UnusedVariable") // Used by VarHandle
  private boolean state;

  private int counter = 0;

  public VarHandleEventualBooleanState() {
    STATE_HANDLE.setRelease(this, false);
  }

  @Override
  public boolean get() {
    if (counter++ > 1000) {
      counter = 0;
      return (boolean) STATE_HANDLE.getAcquire(this);
    }

    return (boolean) STATE_HANDLE.getOpaque(this);
  }

  @Override
  public void set(boolean state) {
    STATE_HANDLE.setRelease(this, state);
  }
}
