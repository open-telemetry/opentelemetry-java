/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

/**
 * All possible instrument types.
 *
 * @since 1.14.0
 */
public enum InstrumentType {
  COUNTER,
  UP_DOWN_COUNTER,
  HISTOGRAM,
  OBSERVABLE_COUNTER,
  OBSERVABLE_UP_DOWN_COUNTER,
  OBSERVABLE_GAUGE,
}
