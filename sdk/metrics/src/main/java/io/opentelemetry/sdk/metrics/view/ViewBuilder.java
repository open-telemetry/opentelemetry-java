/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

/** Raw interface to construct views. */
public interface ViewBuilder {

  /** Select the instrument(s) which will feed measurements to this view. */
  ViewBuilder select(InstrumentSelectionCriteria selection);

  /** The `name` of the View (optional). If not provided, the Instrument `name` will be used. */
  ViewBuilder setName(String name);

  /** Configure the output of this view. */
  ViewBuilder output(MetricOutputConfiguration output);

  /** Builds the {@link View} configuration. */
  View build();
}
