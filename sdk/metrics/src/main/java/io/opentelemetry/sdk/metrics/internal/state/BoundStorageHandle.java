/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.context.Context;

/**
 * A record target for a single timeseries, resolved once via {@link
 * WriteableMetricStorage#bind(io.opentelemetry.api.common.Attributes)}.
 *
 * <p>Records bypass the per-recording attribute processing and series lookup that {@link
 * WriteableMetricStorage#recordLong} / {@link WriteableMetricStorage#recordDouble} perform, since
 * the series is resolved at bind time. Backs bound instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface BoundStorageHandle {

  /** Records a long measurement against the bound series. */
  void recordLong(long value, Context context);

  /** Records a double measurement against the bound series. */
  void recordDouble(double value, Context context);
}
