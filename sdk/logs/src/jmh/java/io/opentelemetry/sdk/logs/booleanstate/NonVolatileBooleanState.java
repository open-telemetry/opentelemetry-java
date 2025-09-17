/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.booleanstate;

public class NonVolatileBooleanState implements BooleanState {

  private boolean state;

  @Override
  public boolean get() {
    return state;
  }

  @Override
  public void set(boolean state) {
    this.state = state;
  }
}
