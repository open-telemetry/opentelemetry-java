/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

enum EmptyBody implements Body {
  INSTANCE;

  @Override
  public String asString() {
    return "";
  }

  @Override
  public Type getType() {
    return Type.EMPTY;
  }
}
