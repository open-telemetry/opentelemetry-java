/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import javax.annotation.concurrent.Immutable;

/**
 * Exemplar data for {@code long} measurements.
 *
 * @since 1.14.0
 */
@Immutable
public interface LongExemplarData extends ExemplarData {
  /** Numerical value of the measurement that was recorded. */
  long getValue();
}
