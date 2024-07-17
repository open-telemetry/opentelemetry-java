/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.common.AnyValue;
import io.opentelemetry.sdk.logs.data.Body;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class AnyValueBody implements Body {

  private final AnyValue<?> value;

  private AnyValueBody(AnyValue<?> value) {
    this.value = value;
  }

  public static Body create(AnyValue<?> value) {
    return new AnyValueBody(value);
  }

  @Override
  public Type getType() {
    return Type.STRING;
  }

  @Override
  public String asString() {
    return value.asString();
  }

  public AnyValue<?> asAnyValue() {
    return value;
  }

  @Override
  public String toString() {
    return "AnyValueBody{" + asString() + "}";
  }
}
