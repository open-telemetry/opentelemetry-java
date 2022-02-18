/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.ObservableDoubleCounter;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.metrics.ObservableDoubleUpDownCounter;
import io.opentelemetry.api.metrics.ObservableLongCounter;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongUpDownCounter;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.state.AsynchronousMetricStorage;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class SdkObservableInstrument<O>
    implements ObservableDoubleCounter,
        ObservableLongCounter,
        ObservableDoubleGauge,
        ObservableLongGauge,
        ObservableDoubleUpDownCounter,
        ObservableLongUpDownCounter {

  private static final Logger logger = Logger.getLogger(SdkObservableInstrument.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final String instrumentName;
  private final List<AsynchronousMetricStorage<?, O>> storages;
  private final Consumer<O> callback;
  private final AtomicBoolean removed = new AtomicBoolean(false);

  SdkObservableInstrument(
      String instrumentName, List<AsynchronousMetricStorage<?, O>> storages, Consumer<O> callback) {
    this.instrumentName = instrumentName;
    this.storages = storages;
    this.callback = callback;
  }

  @Override
  public void close() {
    if (removed.compareAndSet(false, true)) {
      storages.forEach(storage -> storage.removeCallback(callback));
      return;
    }
    throttlingLogger.log(
        Level.WARNING, "Instrument " + instrumentName + " has called close() multiple times.");
  }
}
