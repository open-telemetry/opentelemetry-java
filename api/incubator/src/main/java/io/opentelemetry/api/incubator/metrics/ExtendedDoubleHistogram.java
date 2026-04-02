/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.metrics.DoubleHistogram;

/** Extended {@link DoubleHistogram} with experimental APIs. */
public interface ExtendedDoubleHistogram extends DoubleHistogram {

  // keep this class even if it is empty, since experimental methods may be added in the future.
}
