/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import javax.annotation.Nullable;

abstract class AbstractInstrument {

  private final InstrumentDescriptor descriptor;
  final boolean exemplarsAlwaysOff;

  // All arguments cannot be null because they are checked in the abstract builder classes.
  AbstractInstrument(InstrumentDescriptor descriptor, SdkMeter sdkMeter) {
    this.descriptor = descriptor;
    this.exemplarsAlwaysOff = sdkMeter.isExemplarsAlwaysOff();
  }

  final InstrumentDescriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Returns {@link Context#current()}, or {@link Context#root()} when exemplars are known-off and
   * the caller doesn't otherwise care about propagating a specific context. Used by parameterless
   * record overloads on synchronous instruments to skip a thread-local lookup when the resulting
   * context is only consulted by the exemplar path.
   */
  final Context currentOrRootContext() {
    return exemplarsAlwaysOff ? Context.root() : Context.current();
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
