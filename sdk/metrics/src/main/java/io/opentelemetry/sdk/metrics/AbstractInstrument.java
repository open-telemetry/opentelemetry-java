/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import javax.annotation.Nullable;

abstract class AbstractInstrument {

  private final InstrumentDescriptor descriptor;

  // All arguments cannot be null because they are checked in the abstract builder classes.
  AbstractInstrument(InstrumentDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  final InstrumentDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractInstrument)) {
      return false;
    }

    AbstractInstrument that = (AbstractInstrument) o;

    return descriptor.equals(that.descriptor);
  }

  @Override
  public int hashCode() {
    return descriptor.hashCode();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "{" + "descriptor=" + getDescriptor() + '}';
  }
}
