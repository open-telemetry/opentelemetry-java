/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import java.util.function.Supplier;
import javax.annotation.Nullable;

class MutableSupplier<T> implements Supplier<T> {
  @Nullable private T value;

  @Nullable
  @Override
  public T get() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }
}
