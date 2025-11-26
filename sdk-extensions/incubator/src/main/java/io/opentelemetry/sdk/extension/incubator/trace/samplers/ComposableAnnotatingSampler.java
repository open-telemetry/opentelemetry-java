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

final class ComposableAnnotatingSampler implements ComposableSampler {
  private final ComposableSampler delegate;
  private final Attributes attributes;
  private final String description;

  ComposableAnnotatingSampler(ComposableSampler delegate, Attributes attributes) {
    this.delegate = delegate;
    this.attributes = attributes;

    this.description =
        "ComposableAnnotatingSampler{" + delegate.getDescription() + "," + attributes + "}";
  }

  @Override
  public SamplingIntent getSamplingIntent(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    SamplingIntent intent =
        delegate.getSamplingIntent(parentContext, traceId, name, spanKind, attributes, parentLinks);
    return SamplingIntent.create(
        intent.getThreshold(),
        intent.isThresholdReliable(),
        intent.getAttributes().toBuilder().putAll(this.attributes).build(),
        intent.getTraceStateUpdater());
  }

  @Override
  public String getDescription() {
    return description;
  }
}
