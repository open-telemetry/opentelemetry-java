/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link LongValueObserver}. */
public interface LongValueObserverBuilder
    extends AsynchronousInstrumentBuilder<AsynchronousInstrument.LongResult> {
  @Override
  LongValueObserverBuilder setDescription(String description);

  @Override
  LongValueObserverBuilder setUnit(String unit);

  @Override
  LongValueObserverBuilder setUpdater(Consumer<AsynchronousInstrument.LongResult> updater);

  @Override
  LongValueObserver build();
}
