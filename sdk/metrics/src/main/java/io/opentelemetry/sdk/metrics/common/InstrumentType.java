/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.common;

/** All instrument types available in the metric package. */
@SuppressWarnings("checkstyle")
public enum InstrumentType {
  COUNTER,
  UP_DOWN_COUNTER,
  HISTOGRAM,
  @Deprecated
  OBSERVABLE_SUM,
  OBSERVABLE_COUNTER,
  @Deprecated
  OBSERVABLE_UP_DOWN_SUM,
  OBSERVABLE_UP_DOWN_COUNTER,
  OBSERVABLE_GAUGE,
}
