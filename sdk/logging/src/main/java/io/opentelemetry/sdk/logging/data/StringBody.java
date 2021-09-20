/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logging.data;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
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
  @Nullable
  public abstract String asString();
}
