/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SamplingRulesSampler implements Sampler {

  private static final Logger logger = Logger.getLogger(SamplingRulesSampler.class.getName());

  private final Resource resource;
  private final Sampler fallbackSampler;
  private final SamplingRuleApplier[] ruleAppliers;

  SamplingRulesSampler(
      String clientId,
      Resource resource,
      Sampler fallbackSampler,
      List<GetSamplingRulesResponse.SamplingRuleRecord> rules) {
    this.resource = resource;
    this.fallbackSampler = fallbackSampler;
    ruleAppliers =
        rules.stream()
            .map(GetSamplingRulesResponse.SamplingRuleRecord::getRule)
            // Lower priority value takes precedence so normal ascending sort.
            .sorted(Comparator.comparingInt(GetSamplingRulesResponse.SamplingRule::getPriority))
            .map(rule -> new SamplingRuleApplier(clientId, rule))
            .toArray(SamplingRuleApplier[]::new);
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    for (SamplingRuleApplier applier : ruleAppliers) {
      if (applier.matches(name, attributes, resource)) {
        return applier.shouldSample(
            parentContext, traceId, name, spanKind, attributes, parentLinks);
      }
    }

    // In practice, X-Ray always returns a Default rule that matches all requests so it is a bug in
    // our code or X-Ray to reach here, fallback just in case.
    logger.log(
        Level.FINE,
        "No sampling rule matched the request. "
            + "This is a bug in either the OpenTelemetry SDK or X-Ray.");
    return fallbackSampler.shouldSample(
        parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Override
  public String getDescription() {
    return "XrayRulesSampler{" + Arrays.toString(ruleAppliers) + "}";
  }
}
