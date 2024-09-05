/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

@SuppressWarnings("deprecation") // Implementation of deprecated Body
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
