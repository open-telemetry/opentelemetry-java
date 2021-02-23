/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;
import java.util.function.Supplier;

final class TracezTraceConfigSupplier implements Supplier<SpanLimits>, Sampler {

  private volatile Sampler sampler;
  private volatile SpanLimits activeSpanLimits;

  TracezTraceConfigSupplier() {
    sampler = Sampler.traceIdRatioBased(1.0);
    activeSpanLimits = SpanLimits.getDefault();
  }

  @Override
  public SpanLimits get() {
    return activeSpanLimits;
  }

  Sampler getSampler() {
    return sampler;
  }

  void setSampler(Sampler sampler) {
    this.sampler = sampler;
  }

  void setActiveTraceConfig(SpanLimits spanLimits) {
    activeSpanLimits = spanLimits;
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    return sampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Override
  public String getDescription() {
    return sampler.getDescription();
  }
}
