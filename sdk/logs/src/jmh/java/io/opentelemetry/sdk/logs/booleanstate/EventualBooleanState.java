/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.booleanstate;

public class EventualBooleanState implements BooleanState {

  private volatile boolean state;
  private boolean cached;

  private int counter;

  @Override
  public boolean get() {
    if (counter++ > 1000) {
      counter = 0;
      cached = state; // Update cached value for visibility in this thread
    }
    return cached;
  }

  @Override
  public void set(boolean state) {
    this.state = state;
    this.cached = state; // Update cached value for immediate visibility in this thread
  }
}
