/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link LongUpDownSumObserver}. */
public interface LongUpDownSumObserverBuilder
    extends AsynchronousInstrumentBuilder<AsynchronousInstrument.LongResult> {
  @Override
  LongUpDownSumObserverBuilder setDescription(String description);

  @Override
  LongUpDownSumObserverBuilder setUnit(String unit);

  @Override
  LongUpDownSumObserverBuilder setUpdater(Consumer<AsynchronousInstrument.LongResult> updater);

  @Override
  LongUpDownSumObserver build();
}
