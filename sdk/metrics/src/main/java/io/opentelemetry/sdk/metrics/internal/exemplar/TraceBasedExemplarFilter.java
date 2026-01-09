/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

/**
 * Exemplar sampler that only samples measurements with associated sampled traces.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class TraceBasedExemplarFilter implements ExemplarFilterInternal {

  private static final ExemplarFilterInternal INSTANCE = new TraceBasedExemplarFilter();

  private TraceBasedExemplarFilter() {}

  public static ExemplarFilterInternal getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean shouldSampleMeasurement(long value, Attributes attributes, Context context) {
    return hasSampledTrace(context);
  }

  @Override
  public boolean shouldSampleMeasurement(double value, Attributes attributes, Context context) {
    return hasSampledTrace(context);
  }

  private static boolean hasSampledTrace(Context context) {
    return Span.fromContext(context).getSpanContext().isSampled();
  }

  @Override
  public String toString() {
    return "TraceBasedExemplarFilter";
  }
}
