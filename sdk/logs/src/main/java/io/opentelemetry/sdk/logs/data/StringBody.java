/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class StringBody implements Body {
  StringBody() {}

  static Body create(String stringValue) {
    return new AutoValue_StringBody(stringValue);
  }

  @Override
  public final Type getType() {
    return Type.STRING;
  }

  @Override
  public abstract String asString();
}
