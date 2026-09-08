/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;

/**
 * A record target obtained via {@link
 * WriteableMetricStorage#bind(io.opentelemetry.api.common.Attributes)}, backing bound instruments.
 *
 * <p>Usually a single timeseries resolved once at bind time, in which case records bypass the
 * per-recording attribute processing and series lookup that {@link
 * WriteableMetricStorage#recordLong} / {@link WriteableMetricStorage#recordDouble} perform.
 *
 * <p>When the view's {@link AttributesProcessor} derives attributes from {@link Context} (see
 * {@link AttributesProcessor#usesContext()}), binding cannot fix a series ahead of time; the handle
 * instead re-runs attribute processing and series lookup on every record call, forwarding straight
 * back to the storage's own {@code recordLong}/{@code recordDouble}.
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
