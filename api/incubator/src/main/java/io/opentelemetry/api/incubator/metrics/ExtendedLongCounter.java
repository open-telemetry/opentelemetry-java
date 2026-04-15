/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.LongCounter;

/** Extended {@link DoubleCounter} with experimental APIs. */
public interface ExtendedLongCounter extends LongCounter {

  // keep this class even if it is empty, since experimental methods may be added in the future.
}
