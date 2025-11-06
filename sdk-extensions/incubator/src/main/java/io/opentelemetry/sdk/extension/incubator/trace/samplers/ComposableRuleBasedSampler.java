/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.NON_SAMPLING_INTENT;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.List;

final class ComposableRuleBasedSampler implements ComposableSampler {

  private final SamplingRule[] rules;
  private final String description;

  ComposableRuleBasedSampler(List<SamplingRule> rules) {
    this.rules = rules.toArray(new SamplingRule[0]);

    StringBuilder description = new StringBuilder("ComposableRuleBasedSampler{[");
    if (this.rules.length > 0) {
      for (SamplingRule rule : this.rules) {
        description.append('(');
        description.append(rule.predicate().toString());
        description.append(':');
        description.append(rule.sampler().getDescription());
        description.append(')');
        description.append(',');
      }
      // Remove trailing comma
      description.setLength(description.length() - 1);
    }
    description.append("]}");
    this.description = description.toString();
  }

  @Override
  public SamplingIntent getSamplingIntent(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    for (SamplingRule rule : rules) {
      if (rule.predicate()
          .matches(parentContext, traceId, name, spanKind, attributes, parentLinks)) {
        return rule.sampler()
            .getSamplingIntent(parentContext, traceId, name, spanKind, attributes, parentLinks);
      }
    }
    return NON_SAMPLING_INTENT;
  }

  @Override
  public String getDescription() {
    return description;
  }
}
