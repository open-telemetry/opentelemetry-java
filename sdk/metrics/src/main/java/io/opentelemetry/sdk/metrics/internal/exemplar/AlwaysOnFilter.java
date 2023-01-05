/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

/**
 * A filter which makes all measurements eligible for being an exemplar.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AlwaysOnFilter implements ExemplarFilter {
  static final ExemplarFilter INSTANCE = new AlwaysOnFilter();

  private AlwaysOnFilter() {}

  @Override
  public boolean shouldSampleMeasurement(long value, Attributes attributes, Context context) {
    return true;
  }

  @Override
  public boolean shouldSampleMeasurement(double value, Attributes attributes, Context context) {
    return true;
  }
}
