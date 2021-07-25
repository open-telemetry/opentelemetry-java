/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

/** Exemplar sampler that only samples measuremetns with assocaited sampled traces. */
final class WithTraceExemplarFilter implements ExemplarFilter {

  @Override
  public boolean shouldSampleLongMeasurement(long value, Attributes attributes, Context context) {
    return hasSampledTrace(context);
  }

  @Override
  public boolean shouldSampleDoubleMeasurement(
      double value, Attributes attributes, Context context) {
    return hasSampledTrace(context);
  }

  private static boolean hasSampledTrace(Context context) {
    return Span.fromContext(context).getSpanContext().isSampled();
  }
}
