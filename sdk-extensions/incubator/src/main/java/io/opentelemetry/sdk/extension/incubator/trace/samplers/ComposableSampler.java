/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.List;

/** A sampler that can be composed to make a final sampling decision. */
public interface ComposableSampler {
  /** Returns a {@link ComposableSampler} that does not sample any span. */
  static ComposableSampler alwaysOff() {
    return ComposableAlwaysOffSampler.INSTANCE;
  }

  /** Returns a {@link ComposableSampler} that samples all spans. */
  static ComposableSampler alwaysOn() {
    return ComposableAlwaysOnSampler.INSTANCE;
  }

  /** Returns a {@link ComposableSampler} that samples each span with a fixed ratio. */
  static ComposableSampler probability(double ratio) {
    return new ComposableProbabilitySampler(ratio);
  }

  /**
   * Returns a {@link ComposableSampler} that respects the sampling decision of the parent span or
   * falls back to the given sampler if it is a root span.
   */
  static ComposableSampler parentThreshold(ComposableSampler rootSampler) {
    return new ComposableParentThresholdSampler(rootSampler);
  }

  /**
   * Returns a {@link ComposableRuleBasedSamplerBuilder} to create a composable rule-based sampler.
   * Rules will be tested in order, and the first to match will have its {@link ComposableSampler}
   * used for a sampling decision. If no rule matches, the span will be dropped.
   */
  static ComposableRuleBasedSamplerBuilder ruleBasedBuilder() {
    return new ComposableRuleBasedSamplerBuilder();
  }

  /** Returns the {@link SamplingIntent} to use to make a sampling decision. */
  SamplingIntent getSamplingIntent(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks);

  /** Returns a description of the sampler implementation. */
  String getDescription();
}
