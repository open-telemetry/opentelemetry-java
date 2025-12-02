/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import java.util.ArrayList;
import java.util.List;

/** A builder for a composable rule-based sampler. */
public final class ComposableRuleBasedSamplerBuilder {
  private final List<SamplingRule> rules = new ArrayList<>();

  ComposableRuleBasedSamplerBuilder() {}

  /**
   * Adds a rule to use the given {@link ComposableSampler} if the {@link SamplingPredicate}
   * matches.
   */
  public ComposableRuleBasedSamplerBuilder add(
      SamplingPredicate predicate, ComposableSampler sampler) {
    rules.add(ImmutableSamplingRule.create(predicate, sampler));
    return this;
  }

  /** Returns a {@link ComposableSampler} with the rules in this builder. */
  public ComposableSampler build() {
    return new ComposableRuleBasedSampler(rules);
  }
}
