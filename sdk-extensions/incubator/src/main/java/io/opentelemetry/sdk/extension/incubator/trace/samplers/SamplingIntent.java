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
      boolean thresholdReliable,
      Attributes attributes,
      Function<TraceState, TraceState> traceStateUpdater) {
    return ImmutableSamplingIntent.create(
        threshold, thresholdReliable, attributes, traceStateUpdater);
  }

  /**
   * Returns the sampling threshold value. A lower threshold increases the likelihood of sampling.
   */
  long getThreshold();

  /** Returns whether the threshold can be reliably used for Span-to-Metrics estimation. */
  boolean isThresholdReliable();

  /** Returns any attributes to add to the span to record the sampling result. */
  Attributes getAttributes();

  /** Returns a function to apply to the tracestate of the span to possibly update it. */
  Function<TraceState, TraceState> getTraceStateUpdater();
}
