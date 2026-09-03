/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import javax.annotation.Nullable;

abstract class AbstractInstrument {

  private final InstrumentDescriptor descriptor;

  /**
   * True when the record-path {@link Context} has no observable effect for this instrument, so
   * parameterless record overloads may substitute {@link Context#root()} for {@link
   * Context#current()} and skip a thread-local lookup. Both potential consumers must be inactive:
   * the exemplar filter (disabled meter-wide) and every backing storage's {@link
   * io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor} (e.g. baggage append).
   */
  private final boolean canUseRootContext;

  // All arguments cannot be null because they are checked in the abstract builder classes.
  AbstractInstrument(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    this.descriptor = descriptor;
    this.canUseRootContext = sdkMeter.isExemplarsAlwaysOff() && !storage.usesContext();
  }

  final InstrumentDescriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Returns {@link Context#current()}, or {@link Context#root()} when the current context can have
   * no observable effect on this instrument's outputs (see {@link #canUseRootContext}). Used by
   * parameterless record overloads on synchronous instruments to skip a thread-local lookup.
   */
  final Context currentOrRootContext() {
    return canUseRootContext ? Context.root() : Context.current();
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
