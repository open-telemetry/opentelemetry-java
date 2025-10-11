/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.booleanstate;

public interface BooleanState {

  boolean get();

  void set(boolean state);
}
