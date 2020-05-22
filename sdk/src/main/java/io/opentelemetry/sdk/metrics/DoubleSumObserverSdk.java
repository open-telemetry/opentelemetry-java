/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.metrics.DoubleSumObserver;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

final class DoubleSumObserverSdk extends AbstractAsynchronousInstrument
    implements DoubleSumObserver {
  @Nullable private volatile Callback<ResultDoubleSumObserver> metricUpdater = null;
  private final ReentrantLock collectLock = new ReentrantLock();

  DoubleSumObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(descriptor, meterProviderSharedState, meterSharedState);
  }

  @Override
  List<MetricData> collectAll() {
    Callback<ResultDoubleSumObserver> currentMetricUpdater = metricUpdater;
    if (currentMetricUpdater == null) {
      return Collections.emptyList();
    }
    collectLock.lock();
    try {
      final ActiveBatcher activeBatcher = getActiveBatcher();
      currentMetricUpdater.update(new ResultDoubleSumObserverSdk(activeBatcher));
      return activeBatcher.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }

  @Override
  public void setCallback(Callback<ResultDoubleSumObserver> callback) {
    this.metricUpdater = Objects.requireNonNull(callback, "metricUpdater");
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleSumObserverSdk.Builder>
      implements DoubleSumObserver.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(name, meterProviderSharedState, meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleSumObserverSdk build() {
      return register(
          new DoubleSumObserverSdk(
              getInstrumentDescriptor(InstrumentType.SUM_OBSERVER, InstrumentValueType.DOUBLE),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }

  private static final class ResultDoubleSumObserverSdk implements ResultDoubleSumObserver {

    private final ActiveBatcher activeBatcher;

    private ResultDoubleSumObserverSdk(ActiveBatcher activeBatcher) {
      this.activeBatcher = activeBatcher;
    }

    @Override
    public void observe(double sum, String... keyValueLabelPairs) {
      Aggregator aggregator = activeBatcher.getAggregator();
      aggregator.recordDouble(sum);
      activeBatcher.batch(
          LabelSetSdk.create(keyValueLabelPairs), aggregator, /* mappedAggregator= */ false);
    }
  }
}
