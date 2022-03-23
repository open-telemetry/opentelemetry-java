/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

/** All instrument types available in the metric package. */
public enum InstrumentType {
  COUNTER,
  UP_DOWN_COUNTER,
  HISTOGRAM,
  OBSERVABLE_COUNTER,
  OBSERVABLE_UP_DOWN_COUNTER,
  OBSERVABLE_GAUGE,
}
