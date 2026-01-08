/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class ImmutableSamplingRule implements SamplingRule {
  static final ImmutableSamplingRule create(
      SamplingPredicate predicate, ComposableSampler sampler) {
    return new AutoValue_ImmutableSamplingRule(predicate, sampler);
  }
}
