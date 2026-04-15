/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * This sampler will return the sampling result of the provided {@link #rootSampler}, unless the
 * sampling result contains the sampling decision {@link SamplingDecision#DROP}, in which case, a
 * new sampling result will be returned that is functionally equivalent to the original, except that
 * it contains the sampling decision {@link SamplingDecision#RECORD_ONLY}. This ensures that all
 * spans are recorded, with no change to sampling.
 *
 * <p>An intended use case of this sampler is to provide a means of sending all spans to a processor
 * without having an impact on the sampling rate. This may be desirable if a user wishes to count or
 * otherwise measure all spans produced in a service, without incurring the cost of 100% sampling.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
@Immutable
public final class AlwaysRecordSampler implements Sampler {

  private final Sampler rootSampler;

  public static AlwaysRecordSampler create(Sampler rootSampler) {
    return new AlwaysRecordSampler(rootSampler);
  }

  private AlwaysRecordSampler(Sampler rootSampler) {
    this.rootSampler = rootSampler;
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    SamplingResult result =
        rootSampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    if (result.getDecision() != SamplingDecision.DROP) {
      return result;
    }

    return new RecordOnlyDelegateSamplingResult(result);
  }

  @Override
  public String getDescription() {
    return "AlwaysRecordSampler{" + rootSampler.getDescription() + "}";
  }

  private static class RecordOnlyDelegateSamplingResult implements SamplingResult {
    private final SamplingResult delegate;

    private RecordOnlyDelegateSamplingResult(SamplingResult delegate) {
      this.delegate = delegate;
    }

    @Override
    public SamplingDecision getDecision() {
      return SamplingDecision.RECORD_ONLY;
    }

    @Override
    public Attributes getAttributes() {
      return delegate.getAttributes();
    }

    @Override
    public TraceState getUpdatedTraceState(TraceState parentTraceState) {
      return delegate.getUpdatedTraceState(parentTraceState);
    }
  }
}
