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

enum ComposableAlwaysOnSampler implements ComposableSampler {
  INSTANCE;

  private static final SamplingIntent INTENT =
      SamplingIntent.create(
          ImmutableSamplingIntent.MIN_THRESHOLD,
          /* adjustedCountReliable= */ true,
          Attributes.empty(),
          ImmutableSamplingIntent.NO_TRACE_STATE_UPDATE);

  @Override
  public SamplingIntent getSamplingIntent(
      Context parentContext,
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
