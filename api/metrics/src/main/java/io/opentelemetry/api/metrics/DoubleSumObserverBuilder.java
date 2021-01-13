/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link DoubleSumObserver}. */
public interface DoubleSumObserverBuilder
    extends AsynchronousInstrumentBuilder<AsynchronousInstrument.DoubleResult> {
  @Override
  DoubleSumObserverBuilder setDescription(String description);

  @Override
  DoubleSumObserverBuilder setUnit(String unit);

  @Override
  DoubleSumObserverBuilder setUpdater(Consumer<AsynchronousInstrument.DoubleResult> updater);

  @Override
  DoubleSumObserver build();
}
