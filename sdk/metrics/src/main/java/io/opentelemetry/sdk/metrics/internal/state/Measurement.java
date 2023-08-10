/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;

public interface Measurement {
  long startEpochNanos();

  long epochNanos();

  boolean hasLongValue();

  long longValue();

  boolean hasDoubleValue();

  double doubleValue();

  Attributes attributes();
}
