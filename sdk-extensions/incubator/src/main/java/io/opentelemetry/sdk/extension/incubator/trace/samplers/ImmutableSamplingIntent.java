/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.TraceState;
import java.util.function.Function;

@AutoValue
abstract class ImmutableSamplingIntent implements SamplingIntent {
  private static final int RANDOM_VALUE_BITS = 56;

  static final long INVALID_THRESHOLD = -1;
  static final long INVALID_RANDOM_VALUE = -1;
  static final long MIN_THRESHOLD = 0;
  static final long MAX_THRESHOLD = 1L << RANDOM_VALUE_BITS;
  static final long MAX_RANDOM_VALUE = MAX_THRESHOLD - 1;

  static boolean isValidThreshold(long threshold) {
    return threshold >= MIN_THRESHOLD && threshold <= MAX_THRESHOLD;
  }

  static boolean isValidRandomValue(long randomValue) {
    return randomValue >= 0 && randomValue <= MAX_RANDOM_VALUE;
  }

  static ImmutableSamplingIntent create(
      long threshold,
      boolean thresholdReliable,
      Attributes attributes,
      Function<TraceState, TraceState> traceStateUpdater) {
    return new AutoValue_ImmutableSamplingIntent(
        threshold, thresholdReliable, attributes, traceStateUpdater);
  }
}
