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

enum ComposableAlwaysOffSampler implements ComposableSampler {
  INSTANCE;

  @Override
  public SamplingIntent getSamplingIntent(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    return NON_SAMPLING_INTENT;
  }

  @Override
  public String getDescription() {
    return "ComposableAlwaysOffSampler";
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
