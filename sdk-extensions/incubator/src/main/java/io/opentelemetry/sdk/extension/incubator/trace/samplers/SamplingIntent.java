/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.TraceState;
import java.util.function.Function;

/** Information to make a sampling decision. */
public interface SamplingIntent {

  /** Returns a {@link SamplingIntent} with the given data. */
  static SamplingIntent create(
      long threshold,
      boolean adjustedCountReliable,
      Attributes attributes,
      Function<TraceState, TraceState> traceStateUpdater) {
    return ImmutableSamplingIntent.create(
        threshold, adjustedCountReliable, attributes, traceStateUpdater);
  }

  /**
   * Returns the sampling threshold value. A lower threshold increases the likelihood of sampling.
   */
  long getThreshold();

  /**
   * The threshold provided by the SamplingIntent can always be used to determine the sampling
   * decision. However, in certain situations it cannot be used to calculate the adjusted count
   * (reciprocal of sampling probability, used by Span-to-Metrics estimation) reliably, because a
   * non-consistent-probability sampling decision might have affected the threshold value.
   *
   * @return true iff the threshold can be reliably used for adjusted count calculation
   */
  boolean isAdjustedCountReliable();

  /** Returns any attributes to add to the span to record the sampling result. */
  Attributes getAttributes();

  /** Returns a function to apply to the tracestate of the span to possibly update it. */
  Function<TraceState, TraceState> getTraceStateUpdater();
}
