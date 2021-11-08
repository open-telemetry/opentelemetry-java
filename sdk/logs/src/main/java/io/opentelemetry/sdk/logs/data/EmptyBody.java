/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

class EmptyBody implements Body {

  static final EmptyBody INSTANCE = new EmptyBody();

  private EmptyBody() {}

  @Override
  public String asString() {
    return "";
  }

  @Override
  public Type getType() {
    return Type.EMPTY;
  }
}
