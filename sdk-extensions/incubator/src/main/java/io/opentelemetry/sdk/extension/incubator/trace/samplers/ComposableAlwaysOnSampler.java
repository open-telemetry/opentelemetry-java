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
import java.util.function.Function;

enum ComposableAlwaysOnSampler implements ComposableSampler {
  INSTANCE;

  private static final SamplingIntent INTENT =
      SamplingIntent.create(
          ImmutableSamplingIntent.MIN_THRESHOLD,
          /* thresholdReliable= */ true,
          Attributes.empty(),
          Function.identity());

  @Override
  public SamplingIntent getSamplingIntent(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    return INTENT;
  }

  @Override
  public String getDescription() {
    return "ComposableAlwaysOnSampler";
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
