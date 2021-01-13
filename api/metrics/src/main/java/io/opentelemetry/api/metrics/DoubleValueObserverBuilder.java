/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link DoubleValueObserver}. */
public interface DoubleValueObserverBuilder
    extends AsynchronousInstrumentBuilder<AsynchronousInstrument.DoubleResult> {
  @Override
  DoubleValueObserverBuilder setDescription(String description);

  @Override
  DoubleValueObserverBuilder setUnit(String unit);

  @Override
  DoubleValueObserverBuilder setUpdater(Consumer<AsynchronousInstrument.DoubleResult> updater);

  @Override
  DoubleValueObserver build();
}
