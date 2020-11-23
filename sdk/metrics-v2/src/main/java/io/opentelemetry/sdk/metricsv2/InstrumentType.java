/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

/** All instrument types available in the metric package. */
public enum InstrumentType {
  COUNTER,
  UP_DOWN_COUNTER,
  VALUE_RECORDER,
  SUM_OBSERVER,
  UP_DOWN_SUM_OBSERVER,
  VALUE_OBSERVER,
}
