/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.context.Context;

/** Extended {@link DoubleGauge} with experimental APIs. */
public interface ExtendedDoubleGauge extends DoubleGauge {

  /**
   * Returns {@code true} if the gauge is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #set(double)}, {@link #set(double, Attributes)}, or {@link #set(double,
   * Attributes, Context)}.
   */
  default boolean isEnabled() {
    return true;
  }
}
