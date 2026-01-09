/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

/**
 * A filter which makes no measurements eligible for being an exemplar.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AlwaysOffExemplarFilter implements ExemplarFilterInternal {
  private static final ExemplarFilterInternal INSTANCE = new AlwaysOffExemplarFilter();

  private AlwaysOffExemplarFilter() {}

  public static ExemplarFilterInternal getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean shouldSampleMeasurement(long value, Attributes attributes, Context context) {
    return false;
  }

  @Override
  public boolean shouldSampleMeasurement(double value, Attributes attributes, Context context) {
    return false;
  }

  @Override
  public String toString() {
    return "AlwaysOffExemplarFilter";
  }
}
